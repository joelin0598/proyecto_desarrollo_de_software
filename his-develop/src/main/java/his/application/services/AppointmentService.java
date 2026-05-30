package his.application.services;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.usecases.AppointmentUseCase;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.ports.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentUseCase {

    private static final double CONSULTA_COSTO_Q = 175.0; // RN02

    private final MedicalAppointmentRepository medicalAppointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final MedicalSpecialityRepository specialtyCatalogRepository;
    private final UserRepository userRepository;
    private final PaymentValidationService paymentValidationService;

    @Override
    @Transactional
    public ScheduleAppointmentResponse scheduleAppointment(ScheduleAppointmentRequest request, String emailSolicitante) {
        var currentUser = userRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el usuario autenticado"));

        Patient patient = resolvePatient(request, currentUser.getUserId());
        HospitalStaff doctor = resolveDoctor(request.getMedicoPersonalId());

        validateScheduleRules(request.getFechaCita(), request.getHoraCita());

        if (medicalAppointmentRepository.existsByPacienteIdAndDateTime(
                patient.getPacienteId(),
                request.getFechaCita(),
                request.getHoraCita()
        )) {
            throw new IllegalArgumentException("El paciente ya tiene una cita programada para ese horario.");
        }

        if (medicalAppointmentRepository.existsByPacienteIdAndFecha(
                patient.getPacienteId(),
                request.getFechaCita()
        )) {
            throw new IllegalArgumentException("El paciente ya tiene una cita programada para esa fecha.");
        }

        if (medicalAppointmentRepository.existsByPersonalIdAndDateTime(
                doctor.getPersonalId(),
                request.getFechaCita(),
                request.getHoraCita()
        )) {
            throw new IllegalArgumentException("El medico seleccionado ya tiene una cita en ese horario");
        }

        PaymentValidationService.PaymentValidationResult paymentValidation =
                paymentValidationService.validateForAppointment(request);

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
                .estadoAdministrativo(paymentValidation.approved()
                        ? AdministrativeAppointmentStatus.PAGO_VALIDADO
                        : AdministrativeAppointmentStatus.PAGO_PENDIENTE)
                .observacionAdministrativa(paymentValidation.message())
                .solvenciaPago(paymentValidation.approved())
                .citaProgramada(true)
                .build();

        MedicalAppointment saved = medicalAppointmentRepository.save(appointment);

        // Código/QR se generan una vez existe el ID de cita.
        saved.setCodigoCita(buildAppointmentCode(saved));
        saved.setQrContenido(buildQrPayload(saved, patient));
        saved = medicalAppointmentRepository.save(saved);

        log.info("CU04 cita creada id={} pacienteId={} doctorId={} estadoAdmin={}",
                saved.getCitaMedicaId(),
                saved.getPacienteId(),
                saved.getPersonalId(),
                saved.getEstadoAdministrativo());

        return toResponse(saved, paymentValidation.approved(), paymentValidation.message());
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

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleAppointmentResponse> listAppointments(String emailSolicitante) {
        var currentUser = userRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el usuario autenticado"));

        List<MedicalAppointment> appointments;
        if (currentUser.getRole() == Role.PACIENTE) {
            Patient patient = patientRepository.findByUsuarioId(currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El usuario autenticado no tiene perfil de paciente registrado"));
            appointments = medicalAppointmentRepository.findByPacienteIdOrderByDateTimeDesc(patient.getPacienteId());
        } else {
            appointments = medicalAppointmentRepository.findAllOrderByDateTimeDesc();
        }

        return appointments.stream()
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
            throw new IllegalArgumentException("Debe agendar con al menos 24 horas de anticipacion.");
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

    private ScheduleAppointmentResponse toResponse(MedicalAppointment item, boolean pagoValidado, String message) {
        Patient patient = patientRepository.findById(item.getPacienteId()).orElse(null);
        HospitalStaff doctor = hospitalStaffRepository.findById(item.getPersonalId()).orElse(null);

        Long resolvedSpecialtyId = item.getEspecialidadId() != null
                ? item.getEspecialidadId()
                : (doctor != null ? doctor.getEspecialidadId() : null);

        String specialtyName = null;
        if (resolvedSpecialtyId != null) {
            specialtyName = specialtyCatalogRepository.findById(resolvedSpecialtyId)
                    .map(MedicalSpecialityCatalog::getNombre)
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
                .codigoCita(item.getCodigoCita())
                .qrContenido(item.getQrContenido())
                .mensajeValidacion(message)
                .build();
    }

    private String buildTransactionId(MedicalAppointment item) {
        return "TXN-CITA-" + item.getCitaMedicaId();
    }

    private String buildAppointmentCode(MedicalAppointment item) {
        String datePart = item.getFechaCita() != null
                ? item.getFechaCita().format(DateTimeFormatter.BASIC_ISO_DATE)
                : "SINFECHA";
        return "CITA-" + item.getCitaMedicaId() + "-" + datePart;
    }

    private String buildQrPayload(MedicalAppointment item, Patient patient) {
        String fecha = item.getFechaCita() != null ? item.getFechaCita().toString() : "N/D";
        String hora = item.getHoraCita() != null ? item.getHoraCita().toString() : "N/D";
        String dpi = patient != null && patient.getDpi() != null ? patient.getDpi() : "N/D";
        String nombre = patient != null && patient.getNombreCompleto() != null ? patient.getNombreCompleto() : "N/D";
        return "CITA_ID=" + item.getCitaMedicaId()
                + "|CODIGO=" + item.getCodigoCita()
                + "|FECHA=" + fecha
                + "|HORA=" + hora
                + "|PACIENTE_DPI=" + dpi
                + "|PACIENTE_NOMBRE=" + nombre;
    }

}




