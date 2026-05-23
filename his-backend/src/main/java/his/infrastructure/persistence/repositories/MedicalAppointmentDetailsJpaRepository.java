package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalAppointmentDetailsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalAppointmentDetailsJpaRepository extends JpaRepository<MedicalAppointmentDetailsJpaEntity, Long> {
    Optional<MedicalAppointmentDetailsJpaEntity> findTopByCitaMedicaCitaMedicaIdOrderByCreatedAtDesc(Long citaMedicaId);
}

