package his.application;

import his.application.dto.AppointmentRequest;
import his.application.dto.AppointmentResponse;
import his.domain.Appointment;
import his.domain.AppointmentStatus;
import his.domain.Patient;
import his.domain.ports.AppointmentRepository;
import his.domain.ports.AppointmentUseCase;
import his.domain.ports.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de citas médicas (CU-0).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService implements AppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final AuditService auditService;

    @Override
    @Transactional
    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Paciente no encontrado: " + request.getPatientId()));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctorName(request.getDoctorName())
                .specialty(request.getSpecialty())
                .appointmentDate(request.getAppointmentDate())
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        auditService.log("SCHEDULE_APPOINTMENT", "Appointment", appointment.getAppointmentId(),
                "Cita agendada para " + patient.getFullName() + " con " + request.getDoctorName());

        return mapToResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository
                .findByPatient_PatientIdOrderByAppointmentDateAsc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cita no encontrada: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        auditService.log("CANCEL_APPOINTMENT", "Appointment", appointment.getAppointmentId(),
                "Cita cancelada para " + appointment.getPatient().getFullName());

        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .patient(patientService.mapToResponse(appointment.getPatient()))
                .doctorName(appointment.getDoctorName())
                .specialty(appointment.getSpecialty())
                .appointmentDate(appointment.getAppointmentDate())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
