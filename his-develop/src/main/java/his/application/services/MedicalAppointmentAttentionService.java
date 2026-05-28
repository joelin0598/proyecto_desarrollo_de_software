package his.application.services;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;
import his.application.usecases.MedicalAppointmentAttentionUseCase;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalSpecialityRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalAppointmentAttentionService implements MedicalAppointmentAttentionUseCase {

    private final MedicalAppointmentRepository appointmentRepository;
    private final MedicalAppointmentDetailsRepository appointmentDetailsRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository staffRepository;
    private final MedicalSpecialityRepository specialityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicalAppointmentQueueItemResponse> getPatientQueue(String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);
        List<MedicalAppointment> queue = appointmentRepository.findPendingQueueByDoctor(doctor.getPersonalId());
        if (queue.isEmpty()) {
            log.info("FA02: Cola vacia para doctor personalId={}", doctor.getPersonalId());
        }
        return queue.stream().map(this::toQueueItem).toList();
    }

    @Override
    @Transactional
    public MedicalAppointmentAttentionResponse openAttention(Long citaMedicaId, String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);

        var open = appointmentRepository.findOpenByDoctor(doctor.getPersonalId());
        if (open.isPresent()) {
            log.warn("FA01: Doctor personalId={} ya tiene cita EN_CURSO citaId={}",
                    doctor.getPersonalId(), open.get().getCitaMedicaId());
            throw new IllegalStateException(
                    "El medico ya tiene una cita en curso. Cierre la atencion actual antes de iniciar una nueva.");
        }

        MedicalAppointment cita = appointmentRepository.findById(citaMedicaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita medica con id=" + citaMedicaId));

        if (!doctor.getPersonalId().equals(cita.getPersonalId())) {
            if (!Boolean.FALSE.equals(cita.getCitaProgramada()) && cita.getPersonalId() != null) {
                throw new IllegalArgumentException("La cita no pertenece al medico autenticado.");
            }
        }
        if (cita.getEstadoCita() != StatusAppointment.PROGRAMADA) {
            throw new IllegalArgumentException("La cita ya fue atendida o cancelada.");
        }

        Patient patient = patientRepository.findById(cita.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        cita.setEstadoCita(StatusAppointment.EN_CURSO);
        if (cita.getPersonalId() == null || Boolean.FALSE.equals(cita.getCitaProgramada())) {
            cita.setPersonalId(doctor.getPersonalId());
        }
        MedicalAppointment citaEnCurso = appointmentRepository.save(cita);

        MedicalAppointmentDetails details = appointmentDetailsRepository.save(MedicalAppointmentDetails.builder()
                .citaMedicaId(citaEnCurso.getCitaMedicaId())
                .requiereSeguimiento(false)
                .build());

        log.info("CU06: Atencion abierta detalleId={} citaId={} pacienteId={} doctorId={}",
                details.getMedicalAppointmentDetailsId(), citaEnCurso.getCitaMedicaId(), patient.getPacienteId(), doctor.getPersonalId());

        return toResponse(citaEnCurso, details, patient, doctor,
                resolveEspecialidadNombre(citaEnCurso.getEspecialidadId()),
                resolvePriority(citaEnCurso));
    }

    @Override
    @Transactional
    public MedicalAppointmentAttentionResponse getCurrentAttention(String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);
        MedicalAppointment open = appointmentRepository.findOpenByDoctor(doctor.getPersonalId()).orElse(null);
        if (open == null) {
            return null;
        }

        Patient patient = patientRepository.findById(open.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        MedicalAppointmentDetails details = appointmentDetailsRepository.findByCitaMedicaId(open.getCitaMedicaId())
                .orElseGet(() -> {
                    log.warn("CU06: Cita EN_CURSO sin detalle, se creara detalle tecnico para citaId={}", open.getCitaMedicaId());
                    MedicalAppointmentDetails newDetail = MedicalAppointmentDetails.builder()
                            .citaMedicaId(open.getCitaMedicaId())
                            .requiereSeguimiento(false)
                            .build();
                    MedicalAppointmentDetails saved = appointmentDetailsRepository.save(newDetail);
                    log.info("CU06: Detalle de cita creado exitosamente - detalleId={}, citaId={}",
                            saved.getMedicalAppointmentDetailsId(), open.getCitaMedicaId());
                    return saved;
                });

        if (details.getMedicalAppointmentDetailsId() == null) {
            log.error("CU06: ERROR CRITICO - El detalle de cita no tiene ID asignado despues del guardado. citaId={}", open.getCitaMedicaId());
            throw new IllegalStateException("El sistema no pudo asignar un ID al detalle de la cita. Por favor, intenta nuevamente.");
        }

        return toResponse(open, details, patient, doctor,
                resolveEspecialidadNombre(open.getEspecialidadId()),
                resolvePriority(open));
    }

    @Override
    @Transactional
    public MedicalAppointmentAttentionResponse closeAttention(CloseMedicalAppointmentAttentionRequest req, String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);

        MedicalAppointmentDetails detail = appointmentDetailsRepository.findById(req.getCitaMedicaDetalleId())
                .orElseThrow(() -> new IllegalArgumentException("Detalle de cita no encontrado: " + req.getCitaMedicaDetalleId()));

        MedicalAppointment cita = appointmentRepository.findById(detail.getCitaMedicaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + detail.getCitaMedicaId()));

        if (!doctor.getPersonalId().equals(cita.getPersonalId())) {
            throw new IllegalArgumentException("Esta cita no pertenece al medico autenticado.");
        }
        if (cita.getEstadoCita() != StatusAppointment.EN_CURSO) {
            throw new IllegalStateException("La cita no esta en curso o ya fue finalizada.");
        }

        detail.setEvaluacionFisica(req.getEvaluacionFisica());
        detail.setDiagnostico(req.getDiagnostico());
        detail.setOrdenLaboratorio(req.getOrdenLaboratorio());
        detail.setRecetaMedica(req.getRecetaMedica());
        detail.setMedicacionPrescrita(req.getMedicacionPrescrita());
        detail.setRequiereSeguimiento(req.getRequiereSeguimiento() != null && req.getRequiereSeguimiento());
        MedicalAppointmentDetails closed = appointmentDetailsRepository.save(detail);

        cita.setEstadoCita(StatusAppointment.ATENDIDA);
        MedicalAppointment attended = appointmentRepository.save(cita);

        log.info("CU06: Atencion cerrada detalleId={} citaId={} doctorId={} seguimiento={}",
                closed.getMedicalAppointmentDetailsId(), attended.getCitaMedicaId(), doctor.getPersonalId(), closed.getRequiereSeguimiento());

        Patient patient = patientRepository.findById(attended.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        return toResponse(attended, closed, patient, doctor,
                resolveEspecialidadNombre(attended.getEspecialidadId()),
                resolvePriority(attended));
    }

    @Override
    @Transactional
    public boolean cancelCurrentAttention(String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);

        MedicalAppointment open = appointmentRepository.findOpenByDoctor(doctor.getPersonalId()).orElse(null);
        if (open == null) {
            return false;
        }

        appointmentDetailsRepository.findByCitaMedicaId(open.getCitaMedicaId())
                .ifPresent(detail -> appointmentDetailsRepository.deleteById(detail.getMedicalAppointmentDetailsId()));

        open.setEstadoCita(StatusAppointment.PROGRAMADA);
        // Mantener personalId evita violar NOT NULL en cita_medica.personal_id.
        appointmentRepository.save(open);

        log.info("CU06: Atencion cancelada citaId={} doctorId={}", open.getCitaMedicaId(), doctor.getPersonalId());
        return true;
    }

    private HospitalStaff resolveDoctor(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        HospitalStaff staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No tiene perfil de personal: " + email));
        if (staff.getRol() != Role.DOCTOR) {
            throw new IllegalArgumentException("El usuario autenticado no tiene rol DOCTOR.");
        }
        return staff;
    }

    private String resolveEspecialidadNombre(Long especialidadId) {
        if (especialidadId == null) {
            return null;
        }
        return specialityRepository.findAllActive().stream()
                .filter(s -> s.getEspecialidadMedicaId() != null && s.getEspecialidadMedicaId().equals(especialidadId))
                .map(MedicalSpecialityCatalog::getNombre)
                .findFirst()
                .orElse("Especialidad #" + especialidadId);
    }

    private String resolvePriority(MedicalAppointment appointment) {
        return appointment.getPrioridad() != null ? appointment.getPrioridad().name() : "SIN_TRIAJE";
    }

    private MedicalAppointmentQueueItemResponse toQueueItem(MedicalAppointment apt) {
        Patient patient = patientRepository.findById(apt.getPacienteId()).orElse(null);
        return MedicalAppointmentQueueItemResponse.builder()
                .citaMedicaId(apt.getCitaMedicaId())
                .pacienteId(apt.getPacienteId())
                .pacienteNombre(patient != null ? patient.getNombreCompleto() : "Paciente #" + apt.getPacienteId())
                .pacienteDpi(patient != null ? patient.getDpi() : null)
                .fechaCita(apt.getFechaCita() != null ? apt.getFechaCita().toString() : null)
                .horaCita(apt.getHoraCita() != null ? apt.getHoraCita().toString() : null)
                .motivoConsulta(apt.getMotivoConsulta())
                .especialidadNombre(resolveEspecialidadNombre(apt.getEspecialidadId()))
                .prioridad(resolvePriority(apt))
                .estadoAdministrativo(apt.getEstadoAdministrativo() != null ? apt.getEstadoAdministrativo().name() : null)
                .alertaEmergencia(Boolean.TRUE.equals(apt.getAlertaEmergencia()))
                .tipoAtencion(Boolean.TRUE.equals(apt.getCitaProgramada()) ? "CITA_PROGRAMADA" : "SIN_CITA_PREVIA")
                .presionSistolica(apt.getPresionSistolica())
                .presionDiastolica(apt.getPresionDiastolica())
                .frecuenciaCardiaca(apt.getFrecuenciaCardiaca())
                .temperatura(apt.getTemperatura())
                .saturacionOxigeno(apt.getSaturacionOxigeno())
                .build();
    }

    private MedicalAppointmentAttentionResponse toResponse(MedicalAppointment cita, MedicalAppointmentDetails details,
                                                           Patient patient, HospitalStaff doctor,
                                                           String especialidadNombre, String prioridad) {
        return MedicalAppointmentAttentionResponse.builder()
                .citaMedicaDetalleId(details.getMedicalAppointmentDetailsId())
                .citaMedicaId(cita.getCitaMedicaId())
                .pacienteId(patient.getPacienteId())
                .pacienteNombre(patient.getNombreCompleto())
                .pacienteDpi(patient.getDpi())
                .personalId(doctor.getPersonalId())
                .medicoNombre(doctor.getNombreCompleto())
                .estado(cita.getEstadoCita())
                .evaluacionFisica(details.getEvaluacionFisica())
                .diagnostico(details.getDiagnostico())
                .ordenLaboratorio(details.getOrdenLaboratorio())
                .recetaMedica(details.getRecetaMedica())
                .medicacionPrescrita(details.getMedicacionPrescrita())
                .requiereSeguimiento(details.getRequiereSeguimiento())
                .createdAt(details.getCreatedAt())
                .fechaCita(cita.getFechaCita() != null ? cita.getFechaCita().toString() : null)
                .horaCita(cita.getHoraCita() != null ? cita.getHoraCita().toString() : null)
                .motivoConsulta(cita.getMotivoConsulta())
                .especialidadNombre(especialidadNombre)
                .prioridad(prioridad)
                .build();
    }
}


