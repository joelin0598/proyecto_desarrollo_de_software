package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalPrescriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalPrescriptionJpaRepository extends JpaRepository<MedicalPrescriptionJpaEntity, Long> {
    Optional<MedicalPrescriptionJpaEntity> findTopByCitaMedicaDetalleCitaMedicaDetalleIdOrderByCreatedAtDesc(Long citaMedicaDetalleId);
}

