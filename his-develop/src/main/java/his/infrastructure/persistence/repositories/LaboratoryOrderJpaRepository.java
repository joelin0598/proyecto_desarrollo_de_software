package his.infrastructure.persistence.repositories;

import his.domain.models.LaboratoryOrderStatus;
import his.infrastructure.persistence.entities.LaboratoryOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratoryOrderJpaRepository extends JpaRepository<LaboratoryOrderJpaEntity, Long> {
    List<LaboratoryOrderJpaEntity> findByCitaMedicaDetalleCitaMedicaDetalleIdOrderByCreatedAtDesc(Long citaMedicaDetalleId);
    List<LaboratoryOrderJpaEntity> findByEstadoAndIsActiveTrue(LaboratoryOrderStatus estado);
}

