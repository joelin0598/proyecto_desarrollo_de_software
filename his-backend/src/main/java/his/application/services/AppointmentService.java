package his.application.services;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.usecases.AppointmentUseCase;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicallSpecialtyCatalog;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.InsuranceCatalogRepository;
import his.domain.ports.MedicalSpecialtyCatalogRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentUseCase {

    private static final double CONSULTA_COSTO_Q = 175.0; // RN02

    private final MedicalAppointmentRepository medicalAppointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final MedicalSpecialtyCatalogRepository specialtyCatalogRepository;
    private final InsuranceCatalogRepository insuranceCatalogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ScheduleAppointmentResponse scheduleAppointment(ScheduleAppointmentRequest request, String emailSolicitante) {
        var currentUser = userRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el usuario autenticado"));

        Patient patient = resolvePatient(request, currentUser.getUserId());
        HospitalStaff doctor = resolveDoctor(request.getMedicoPersonalId());

        validateScheduleRules(request.getFechaCita(), request.getHoraCita());

        if (medicalAppointmentRepository.existsByPersonalIdAndDateTime(
                doctor.getPersonalId(),
                request.getFechaCita(),
                request.getHoraCita()
        )) {
            throw new IllegalArgumentException("El medico seleccionado ya tiene una cita en ese horario");
        }

        PaymentValidationResult paymentValidation = validateAndSimulatePayment(request);

        MedicalAppointment appointment = MedicalAppointment.builder()
                .pacienteId(patient.getPacienteId())
                .personalId(doctor.getPersonalId())
                .especialidadId(request.getEspecialidadId())
                .fechaCita(request.getFechaCita())
                .horaCita(request.getHoraCita())
                .motivoConsulta(request.getMotivoConsulta().trim())
                .metodoPago(request.getMetodoPago())
                .costoConsulta(CONSULTA_COSTO_Q)
                .estadoCita(StatusAppointment.PROGRAMADA)
                .estadoAdministrativo(paymentValidation.approved
                        ? AdministrativeAppointmentStatus.PAGO_VALIDADO
                        : AdministrativeAppointmentStatus.PAGO_PENDIENTE)
                .observacionAdministrativa(paymentValidation.message)
                .build();

        MedicalAppointment saved = medicalAppointmentRepository.save(appointment);

        log.info("CU04 cita creada id={} pacienteId={} doctorId={} estadoAdmin={}",
                saved.getCitaMedicaId(),
                saved.getPacienteId(),
                saved.getPersonalId(),
                saved.getEstadoAdministrativo());

        return toResponse(saved, paymentValidation.approved, paymentValidation.message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleAppointmentResponse> listAppointments() {
        return medicalAppointmentRepository.findAllOrderByDateTimeDesc().stream()
                .map(item -> toResponse(
                        item,
                        item.getEstadoAdministrativo() == AdministrativeAppointmentStatus.PAGO_VALIDADO,
                        item.getObservacionAdministrativa()))
                .toList();
    }

    private Patient resolvePatient(ScheduleAppointmentRequest request, Long usuarioIdSolicitante) {
        // Si no se envía pacienteId ni dpiPaciente, derivar el paciente del usuario autenticado (portal paciente)
        if (request.getPacienteId() == null && (request.getDpiPaciente() == null || request.getDpiPaciente().isBlank())) {
            return patientRepository.findByUsuarioId(usuarioIdSolicitante)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El usuario autenticado no tiene perfil de paciente registrado"));
        }

        Patient patient;
        if (request.getPacienteId() != null) {
            patient = patientRepository.findById(request.getPacienteId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe paciente con el ID indicado"));
        } else {
            patient = patientRepository.findByDpi(request.getDpiPaciente())
                    .orElseThrow(() -> new IllegalArgumentException("No existe paciente con el DPI indicado"));
        }

        if (request.getDpiPaciente() != null && !request.getDpiPaciente().isBlank()
                && patient.getDpi() != null
                && !patient.getDpi().equals(request.getDpiPaciente())) {
            throw new IllegalArgumentException("El pacienteId y el dpiPaciente no coinciden entre si");
        }

        return patient;
    }

    private HospitalStaff resolveDoctor(Long doctorId) {
        HospitalStaff doctor = hospitalStaffRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe personal hospitalario con el medicoPersonalId enviado"));

        if (doctor.getRol() != Role.DOCTOR) {
            throw new IllegalArgumentException("El personal seleccionado no tiene rol DOCTOR");
        }

        return doctor;
    }

    private void validateScheduleRules(java.time.LocalDate fechaCita, LocalTime horaCita) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(fechaCita, horaCita);
        LocalDateTime now = LocalDateTime.now();

        // RN05: minimo 24 horas de anticipacion
        if (appointmentDateTime.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("La cita debe programarse con al menos 24 horas de anticipacion");
        }

        // RN05: horario institucional 08:00 a 16:30
        if (horaCita.isBefore(LocalTime.of(8, 0)) || horaCita.isAfter(LocalTime.of(16, 30))) {
            throw new IllegalArgumentException("La cita debe estar en horario de 08:00 a 16:30");
        }

        // RN05: intervalos de 30 minutos exactos
        boolean validMinute = horaCita.getMinute() == 0 || horaCita.getMinute() == 30;
        if (!validMinute || horaCita.getSecond() != 0 || horaCita.getNano() != 0) {
            throw new IllegalArgumentException("La cita solo permite intervalos de 30 minutos (HH:00 o HH:30)");
        }
    }

    private PaymentValidationResult validateAndSimulatePayment(ScheduleAppointmentRequest request) {
        if (request.getMetodoPago() == PaymentOption.TARJETA) {
            return simulateCardAuthorization(request);
        }
        if (request.getMetodoPago() == PaymentOption.SEGURO) {
            return simulateInsuranceCoverage(request);
        }
        throw new IllegalArgumentException("Metodo de pago no soportado");
    }

    private PaymentValidationResult simulateCardAuthorization(ScheduleAppointmentRequest request) {
        if (isBlank(request.getBancoTarjeta())
                || isBlank(request.getNumeroTarjeta())
                || isBlank(request.getFechaVencimientoTarjeta())
                || isBlank(request.getNombreTitularTarjeta())
                || isBlank(request.getCvc())) {
            throw new IllegalArgumentException("Para pago con tarjeta debe completar banco, numero, vencimiento, titular y CVC");
        }

        YearMonth expiry = parseExpiry(request.getFechaVencimientoTarjeta().trim());
        YearMonth now = YearMonth.now();
        if (expiry.isBefore(now)) {
            throw new IllegalArgumentException("La fecha de vencimiento de la tarjeta no puede estar expirada");
        }

        // Simulacion RN07: si termina en 0000, se considera saldo insuficiente.
        boolean approved = !request.getNumeroTarjeta().trim().endsWith("0000");
        if (approved) {
            return new PaymentValidationResult(true, "Pago con tarjeta validado correctamente");
        }
        return new PaymentValidationResult(false, "Pago con tarjeta pendiente: simulacion de saldo insuficiente");
    }

    private PaymentValidationResult simulateInsuranceCoverage(ScheduleAppointmentRequest request) {
        if (request.getAseguradoraId() == null || isBlank(request.getNumeroPoliza())) {
            throw new IllegalArgumentException("Para cobertura de seguro debe enviar aseguradoraId y numeroPoliza");
        }

        insuranceCatalogRepository.findById(request.getAseguradoraId())
                .orElseThrow(() -> new IllegalArgumentException("La aseguradora enviada no existe o no esta activa"));

        // Simulacion RN07: polizas que inicien con X o contengan RECHAZADA marcan rechazo.
        String policy = request.getNumeroPoliza().trim().toUpperCase();
        boolean approved = !(policy.startsWith("X") || policy.contains("RECHAZADA"));

        if (approved) {
            return new PaymentValidationResult(true, "Cobertura de seguro validada correctamente");
        }
        return new PaymentValidationResult(false, "Cobertura pendiente: simulacion de poliza no vigente/no cubierta");
    }

    private YearMonth parseExpiry(String expiry) {
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            return YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("La fecha de vencimiento debe tener formato MM/yy");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ScheduleAppointmentResponse toResponse(MedicalAppointment item, boolean pagoValidado, String message) {
        Patient patient = patientRepository.findById(item.getPacienteId()).orElse(null);
        HospitalStaff doctor = hospitalStaffRepository.findById(item.getPersonalId()).orElse(null);

        Long resolvedSpecialtyId = item.getEspecialidadId() != null
                ? item.getEspecialidadId()
                : (doctor != null ? doctor.getEspecialidadId() : null);

        String specialtyName = null;
        if (resolvedSpecialtyId != null) {
            specialtyName = specialtyCatalogRepository.findById(resolvedSpecialtyId)
                    .map(MedicallSpecialtyCatalog::getNombre)
                    .orElse(null);
        }

        return ScheduleAppointmentResponse.builder()
                .citaMedicaId(item.getCitaMedicaId())
                .pacienteId(item.getPacienteId())
                .pacienteNombre(patient != null ? patient.getNombreCompleto() : null)
                .pacienteIdentificacion(patient != null ? patient.getDpi() : null)
                .medicoPersonalId(item.getPersonalId())
                .medicoNombre(doctor != null ? doctor.getNombreCompleto() : null)
                .especialidadId(resolvedSpecialtyId)
                .especialidadNombre(specialtyName)
                .fechaCita(item.getFechaCita())
                .horaCita(item.getHoraCita())
                .motivoConsulta(item.getMotivoConsulta())
                .metodoPago(item.getMetodoPago())
                .costoConsulta(item.getCostoConsulta())
                .estadoCita(item.getEstadoCita())
                .estadoAdministrativo(item.getEstadoAdministrativo())
                .pagoValidado(pagoValidado)
                .transaccionId(buildTransactionId(item))
                .mensajeValidacion(message)
                .build();
    }

    private String buildTransactionId(MedicalAppointment item) {
        return "TXN-CITA-" + item.getCitaMedicaId();
    }

    private record PaymentValidationResult(boolean approved, String message) {
    }
}




