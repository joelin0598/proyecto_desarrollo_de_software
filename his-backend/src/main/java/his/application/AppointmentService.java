package his.application;

import his.application.dto.AppointmentRequest;
import his.application.dto.AppointmentResponse;
import his.domain.AppointmentEntity;
import his.domain.AppointmentStatus;
import his.domain.UserEntity;
import his.domain.ports.AppointmentRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que implementa el CU-04: Gestión de Citas y Validación de Cobertura de Seguros.
 *
 * <p>Flujo Normal:
 * <ol>
 *   <li>El usuario selecciona especialidad, médico y horario.</li>
 *   <li>El sistema valida disponibilidad y solicita datos de la cita.</li>
 *   <li>Si se proporciona datos de seguro, se consulta al proveedor externo (simulado).</li>
 *   <li>Se aplica el deducible o tarifa base según resultado.</li>
 *   <li>Se reserva la cita y se registra en la bitácora.</li>
 * </ol>
 *
 * <p>FA01 – Falla de comunicación con aseguradora: aplica tarifa base.
 * <p>FA02 – Cobertura rechazada: estado PENDING_PAYMENT, requiere pago alternativo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private static final double BASE_TARIFF = 350.00;
    private static final double DEDUCTIBLE = 75.00;

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    /**
     * Programa una nueva cita médica para el paciente autenticado.
     *
     * @param request  Datos de la cita e información de seguro
     * @param patientId ID del paciente autenticado
     * @return AppointmentResponse con la cita registrada y estado de cobertura
     */
    @Transactional
    public AppointmentResponse scheduleAppointment(AppointmentRequest request, Long patientId) {
        UserEntity patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        AppointmentStatus status;
        Double baseTariff = BASE_TARIFF;
        Double deductible = 0.0;
        String auditNote;

        boolean hasInsurance = request.getInsurerName() != null
                && !request.getInsurerName().isBlank()
                && request.getPolicyNumber() != null
                && !request.getPolicyNumber().isBlank();

        if (hasInsurance) {
            InsuranceValidationResult result = validateInsuranceCoverage(
                    request.getInsurerName(), request.getPolicyNumber(), request.getHolderDpi());

            switch (result) {
                case APPROVED:
                    status = AppointmentStatus.VALIDATED;
                    deductible = DEDUCTIBLE;
                    auditNote = String.format(
                            "Cobertura validada. Aseguradora: %s. Póliza: %s. Deducible aplicado: Q%.2f.",
                            request.getInsurerName(), request.getPolicyNumber(), deductible);
                    log.info("Cobertura de seguro aprobada para paciente {}", patientId);
                    break;
                case TIMEOUT:
                    // FA01: Falla de comunicación – aplica tarifa base provisional
                    status = AppointmentStatus.PENDING_PAYMENT;
                    auditNote = String.format(
                            "FA01 – Sin respuesta de aseguradora '%s'. Se aplica tarifa base provisional: Q%.2f.",
                            request.getInsurerName(), BASE_TARIFF);
                    log.warn("FA01 – Timeout en consulta de seguro para paciente {}", patientId);
                    break;
                default:
                    // FA02: Cobertura rechazada o insuficiente
                    status = AppointmentStatus.PENDING_PAYMENT;
                    auditNote = String.format(
                            "FA02 – Cobertura rechazada por aseguradora '%s'. Póliza '%s' no vigente o no cubre la especialidad '%s'. "
                                    + "Se requiere método de pago alternativo.",
                            request.getInsurerName(), request.getPolicyNumber(), request.getSpecialty());
                    log.warn("FA02 – Cobertura rechazada para paciente {}", patientId);
                    break;
            }
        } else {
            status = AppointmentStatus.PENDING_PAYMENT;
            auditNote = String.format(
                    "Cita programada sin cobertura de seguro. Tarifa base: Q%.2f. Pendiente de pago.", BASE_TARIFF);
            log.info("Cita programada sin seguro para paciente {}", patientId);
        }

        AppointmentEntity appointment = AppointmentEntity.builder()
                .patient(patient)
                .specialty(request.getSpecialty())
                .doctorName(request.getDoctorName())
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .reason(request.getReason())
                .insurerName(request.getInsurerName())
                .policyNumber(request.getPolicyNumber())
                .holderDpi(request.getHolderDpi())
                .status(status)
                .baseTariff(baseTariff)
                .deductible(deductible)
                .auditNote(auditNote)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Cita {} registrada para paciente {} con estado {}", appointment.getId(), patientId, status);

        String message = buildUserMessage(status, hasInsurance, request.getInsurerName());
        return mapToResponse(appointment, message);
    }

    /**
     * Obtiene todas las citas del paciente autenticado.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatient_UserId(patientId)
                .stream()
                .map(a -> mapToResponse(a, null))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las citas con estado PENDING_PAYMENT (para uso del recepcionista/admin).
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPendingPaymentAppointments() {
        return appointmentRepository.findByStatus(AppointmentStatus.PENDING_PAYMENT)
                .stream()
                .map(a -> mapToResponse(a, null))
                .collect(Collectors.toList());
    }

    /**
     * Simula la validación de cobertura de seguro con un proveedor externo.
     *
     * <p>Reglas de simulación:
     * <ul>
     *   <li>Si el nombre de aseguradora contiene "INVALIDA" o "RECHAZADA" → REJECTED</li>
     *   <li>Si el nombre de aseguradora contiene "TIMEOUT" → TIMEOUT (FA01)</li>
     *   <li>En cualquier otro caso → APPROVED</li>
     * </ul>
     */
    private InsuranceValidationResult validateInsuranceCoverage(String insurerName, String policyNumber, String holderDpi) {
        if (insurerName == null) return InsuranceValidationResult.REJECTED;

        String upperName = insurerName.toUpperCase();
        if (upperName.contains("TIMEOUT")) {
            return InsuranceValidationResult.TIMEOUT;
        }
        if (upperName.contains("INVALIDA") || upperName.contains("RECHAZADA")) {
            return InsuranceValidationResult.REJECTED;
        }
        return InsuranceValidationResult.APPROVED;
    }

    private String buildUserMessage(AppointmentStatus status, boolean hasInsurance, String insurerName) {
        return switch (status) {
            case VALIDATED -> "✅ Cita programada exitosamente. Cobertura de seguro validada con " + insurerName + ".";
            case PENDING_PAYMENT -> hasInsurance
                    ? "⚠️ Cita programada. La cobertura de seguro no pudo ser confirmada. Se requiere validación de pago."
                    : "✅ Cita programada. Pendiente de pago en caja antes de la consulta.";
            default -> "Cita procesada.";
        };
    }

    private AppointmentResponse mapToResponse(AppointmentEntity a, String message) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient() != null ? a.getPatient().getUserId() : null)
                .patientName(a.getPatient() != null
                        ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "")
                .specialty(a.getSpecialty())
                .doctorName(a.getDoctorName())
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .reason(a.getReason())
                .insurerName(a.getInsurerName())
                .policyNumber(a.getPolicyNumber())
                .status(a.getStatus() != null ? a.getStatus().name() : null)
                .baseTariff(a.getBaseTariff())
                .deductible(a.getDeductible())
                .createdAt(a.getCreatedAt())
                .auditNote(a.getAuditNote())
                .message(message)
                .build();
    }

    private enum InsuranceValidationResult {
        APPROVED, REJECTED, TIMEOUT
    }
}
