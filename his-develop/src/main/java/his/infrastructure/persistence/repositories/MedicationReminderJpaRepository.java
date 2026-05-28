package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicationReminderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationReminderJpaRepository extends JpaRepository<MedicationReminderJpaEntity, Long> {
    List<MedicationReminderJpaEntity> findByPacientePacienteIdAndActivoTrueOrderByProximoRecordatorioAsc(Long pacienteId);
}

