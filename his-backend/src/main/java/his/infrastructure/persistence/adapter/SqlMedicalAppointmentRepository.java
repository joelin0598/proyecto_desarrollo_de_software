package his.infrastructure.persistence.adapter;

import his.domain.models.MedicalAppointment;
import his.domain.ports.MedicalAppointmentRepository;
import his.infrastructure.persistence.mapper.MedicalAppointmentMapper;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SqlMedicalAppointmentRepository implements MedicalAppointmentRepository {

    private final MedicalAppointmentJpaRepository medicalAppointmentJpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;

    @Override
    public MedicalAppointment save(MedicalAppointment appointment) {
        var paciente = patientJpaRepository.getReferenceById(appointment.getPacienteId());
        var personal = hospitalStaffJpaRepository.getReferenceById(appointment.getPersonalId());

        var saved = medicalAppointmentJpaRepository.save(
                MedicalAppointmentMapper.toEntity(appointment, paciente, personal)
        );
        return MedicalAppointmentMapper.toDomain(saved);
    }

    @Override
    public boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita) {
        return medicalAppointmentJpaRepository.existsByPersonalPersonalIdAndFechaCitaAndHoraCitaAndIsActiveTrue(
                personalId,
                fechaCita,
                horaCita
        );
    }

    @Override
    public List<MedicalAppointment> findAllOrderByDateTimeDesc() {
        return medicalAppointmentJpaRepository.findAllByOrderByFechaCitaDescHoraCitaDesc().stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }
}
