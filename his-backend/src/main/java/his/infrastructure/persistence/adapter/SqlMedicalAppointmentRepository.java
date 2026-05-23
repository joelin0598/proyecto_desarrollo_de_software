package his.infrastructure.persistence.adapter;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.MedicalAppointment;
import his.domain.models.StatusAppointment;
import his.domain.ports.MedicalAppointmentRepository;
import his.infrastructure.persistence.mapper.MedicalAppointmentMapper;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.MedicalSpecialityJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalAppointmentRepository implements MedicalAppointmentRepository {

    private final MedicalAppointmentJpaRepository medicalAppointmentJpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;
    private final MedicalSpecialityJpaRepository especialidadJpaRepository;

    @Override
    public MedicalAppointment save(MedicalAppointment appointment) {
        var paciente = patientJpaRepository.getReferenceById(appointment.getPacienteId());
        var personal = appointment.getPersonalId() != null
                ? hospitalStaffJpaRepository.getReferenceById(appointment.getPersonalId())
                : null;
        var especialidad = appointment.getEspecialidadId() != null
                ? especialidadJpaRepository.getReferenceById(appointment.getEspecialidadId())
                : null;
        var saved = medicalAppointmentJpaRepository.save(
                MedicalAppointmentMapper.toEntity(appointment, paciente, personal, especialidad));
        return MedicalAppointmentMapper.toDomain(saved);
    }

    @Override
    public boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita) {
        return medicalAppointmentJpaRepository
                .existsByPersonalPersonalIdAndFechaCitaAndHoraCitaAndIsActiveTrue(personalId, fechaCita, horaCita);
    }

    @Override
    public List<MedicalAppointment> findAllOrderByDateTimeDesc() {
        return medicalAppointmentJpaRepository.findAllByOrderByFechaCitaDescHoraCitaDesc().stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<MedicalAppointment> findByPacienteIdOrderByDateTimeDesc(Long pacienteId) {
        return medicalAppointmentJpaRepository.findByPacientePacienteIdOrderByFechaCitaDescHoraCitaDesc(pacienteId)
                .stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MedicalAppointment> findById(Long citaMedicaId) {
        return medicalAppointmentJpaRepository.findById(citaMedicaId)
                .map(MedicalAppointmentMapper::toDomain);
    }

    @Override
    public Optional<MedicalAppointment> findOpenByDoctor(Long personalId) {
        return medicalAppointmentJpaRepository
                .findTopByPersonalPersonalIdAndEstadoCitaAndIsActiveTrueOrderByUpdatedAtDesc(
                        personalId,
                        StatusAppointment.EN_CURSO)
                .map(MedicalAppointmentMapper::toDomain);
    }

    @Override
    public List<MedicalAppointment> findPendingQueueByDoctor(Long personalId) {
        return medicalAppointmentJpaRepository
                .findPendingAttentionQueue(personalId)
                .stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MedicalAppointment> findNextPaidScheduledByPatient(Long pacienteId) {
        return medicalAppointmentJpaRepository
                .findTopByPacientePacienteIdAndEstadoCitaAndEstadoAdministrativoAndIsActiveTrueOrderByFechaCitaAscHoraCitaAsc(
                        pacienteId,
                        StatusAppointment.PROGRAMADA,
                        AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .map(MedicalAppointmentMapper::toDomain);
    }
}
