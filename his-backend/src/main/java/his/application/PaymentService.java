package his.application;

import his.application.dto.AppointmentResponse;
import his.application.dto.PaymentRequest;
import his.application.dto.PaymentResponse;
import his.domain.AppointmentEntity;
import his.domain.AppointmentStatus;
import his.domain.PaymentEntity;
import his.domain.PaymentMethod;
import his.domain.PaymentStatus;
import his.domain.ports.AppointmentRepository;
import his.domain.ports.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que implementa el CU-05: Verificación de Pago y Filtro de Atención.
 *
 * <p>Flujo Normal:
 * <ol>
 *   <li>El recepcionista visualiza la lista de pacientes pendientes de pago.</li>
 *   <li>El recepcionista registra la transacción de cobro.</li>
 *   <li>El sistema valida la solvencia (RN03) y cambia el estado a "Habilitado para Clínica" (VALIDATED).</li>
 *   <li>El sistema genera el comprobante y registra el evento en bitácora (RN05).</li>
 * </ol>
 *
 * <p>FA01 – Insuficiencia de fondos o rechazo de seguro: bloquea el avance.
 * <p>FA02 – Código Rojo: permite paso inmediato a clínica, pago posterior.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Obtiene la lista de pacientes con citas pendientes de pago.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientsAwaitingPayment() {
        return appointmentRepository.findByStatus(AppointmentStatus.PENDING_PAYMENT)
                .stream()
                .map(this::mapAppointmentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Registra una transacción de cobro y actualiza el estado administrativo del paciente.
     *
     * @param request Datos del cobro (método, monto, autorización, factura)
     * @return PaymentResponse con resultado de la transacción
     */
    @Transactional
    public PaymentResponse registerPayment(PaymentRequest request) {
        AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + request.getAppointmentId()));

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Método de pago inválido: " + request.getPaymentMethod()
                    + ". Valores válidos: CASH, CARD, INSURANCE");
        }

        boolean isEmergencyBypass = Boolean.TRUE.equals(request.getEmergencyBypass());
        PaymentStatus paymentStatus;
        AppointmentStatus appointmentStatus;
        String auditNote;

        if (isEmergencyBypass) {
            // FA02: Código Rojo – paso inmediato a clínica, pago diferido
            paymentStatus = PaymentStatus.PENDING;
            appointmentStatus = AppointmentStatus.VALIDATED;
            auditNote = String.format(
                    "FA02 – Emergencia Código Rojo. Paciente habilitado para clínica sin validación de pago. "
                            + "Trámite administrativo diferido. Responsable: recepcionista. Método: %s.",
                    method.name());
            log.warn("FA02 – Código Rojo: paciente {} habilitado sin pago en cita {}",
                    appointment.getPatient().getUserId(), appointment.getId());
        } else {
            // Validar solvencia (RN03)
            boolean isSolvent = validateSolvency(method, request.getTotalAmount(),
                    request.getAuthorizationNumber(), request.getInsuranceCoverage());

            if (!isSolvent) {
                // FA01: Insuficiencia de fondos o rechazo de seguro
                paymentStatus = PaymentStatus.BLOCKED;
                appointmentStatus = appointment.getStatus(); // mantiene PENDING_PAYMENT
                auditNote = String.format(
                        "FA01 – Pago rechazado. Método: %s. Monto presentado: Q%.2f. "
                                + "Paciente derivado a Trabajo Social o reprogramación.",
                        method.name(), request.getTotalAmount() != null ? request.getTotalAmount() : 0.0);
                log.warn("FA01 – Pago rechazado en cita {} para paciente {}",
                        appointment.getId(), appointment.getPatient().getUserId());
            } else {
                // Pago aceptado – habilitar para clínica
                paymentStatus = PaymentStatus.PAID;
                appointmentStatus = AppointmentStatus.VALIDATED;
                auditNote = String.format(
                        "Pago registrado. Método: %s. Monto: Q%.2f. Cobertura seguro: Q%.2f. "
                                + "Saldo pendiente: Q%.2f. Factura: %s. Paciente habilitado para clínica.",
                        method.name(),
                        request.getTotalAmount(),
                        request.getInsuranceCoverage() != null ? request.getInsuranceCoverage() : 0.0,
                        request.getPendingBalance() != null ? request.getPendingBalance() : 0.0,
                        request.getInvoiceNumber());
                log.info("Pago exitoso en cita {} para paciente {}", appointment.getId(), appointment.getPatient().getUserId());
            }
        }

        // Actualizar estado de la cita
        appointment.setStatus(appointmentStatus);
        appointmentRepository.save(appointment);

        PaymentEntity payment = PaymentEntity.builder()
                .appointment(appointment)
                .paymentMethod(method)
                .authorizationNumber(request.getAuthorizationNumber())
                .totalAmount(request.getTotalAmount())
                .insuranceCoverage(request.getInsuranceCoverage() != null ? request.getInsuranceCoverage() : 0.0)
                .pendingBalance(request.getPendingBalance() != null ? request.getPendingBalance() : 0.0)
                .invoiceNumber(request.getInvoiceNumber())
                .paymentStatus(paymentStatus)
                .emergencyBypass(isEmergencyBypass)
                .auditNote(auditNote)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Pago {} registrado con estado {}", payment.getId(), paymentStatus);

        String message = buildPaymentMessage(paymentStatus, isEmergencyBypass);
        return mapToPaymentResponse(payment, appointment, message);
    }

    /**
     * Valida la solvencia del pago según el método y monto.
     *
     * <p>Reglas (RN03):
     * <ul>
     *   <li>CASH: monto debe ser mayor a cero.</li>
     *   <li>CARD: monto mayor a cero y número de autorización no vacío.</li>
     *   <li>INSURANCE: cobertura mayor a cero y código de aprobación no vacío.</li>
     * </ul>
     */
    private boolean validateSolvency(PaymentMethod method, Double totalAmount,
                                      String authorizationNumber, Double insuranceCoverage) {
        if (totalAmount == null || totalAmount <= 0) return false;

        return switch (method) {
            case CASH -> true;
            case CARD -> authorizationNumber != null && !authorizationNumber.isBlank();
            case INSURANCE -> insuranceCoverage != null && insuranceCoverage > 0
                    && authorizationNumber != null && !authorizationNumber.isBlank();
        };
    }

    private String buildPaymentMessage(PaymentStatus status, boolean isEmergency) {
        if (isEmergency) {
            return "🚨 Código Rojo: Paciente habilitado para clínica de forma inmediata. Trámite de pago diferido.";
        }
        return switch (status) {
            case PAID -> "✅ Pago registrado exitosamente. Paciente habilitado para pasar a clínica.";
            case BLOCKED -> "❌ Pago rechazado. Paciente derivado a Trabajo Social o reprogramación de cita.";
            default -> "⚠️ Estado de pago pendiente.";
        };
    }

    private AppointmentResponse mapAppointmentToResponse(AppointmentEntity a) {
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
                .build();
    }

    private PaymentResponse mapToPaymentResponse(PaymentEntity p, AppointmentEntity a, String message) {
        return PaymentResponse.builder()
                .id(p.getId())
                .appointmentId(a.getId())
                .patientId(a.getPatient() != null ? a.getPatient().getUserId() : null)
                .patientName(a.getPatient() != null
                        ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : "")
                .specialty(a.getSpecialty())
                .doctorName(a.getDoctorName())
                .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                .authorizationNumber(p.getAuthorizationNumber())
                .totalAmount(p.getTotalAmount())
                .insuranceCoverage(p.getInsuranceCoverage())
                .pendingBalance(p.getPendingBalance())
                .invoiceNumber(p.getInvoiceNumber())
                .paymentStatus(p.getPaymentStatus() != null ? p.getPaymentStatus().name() : null)
                .appointmentStatus(a.getStatus() != null ? a.getStatus().name() : null)
                .emergencyBypass(p.getEmergencyBypass())
                .createdAt(p.getCreatedAt())
                .auditNote(p.getAuditNote())
                .message(message)
                .build();
    }
}
