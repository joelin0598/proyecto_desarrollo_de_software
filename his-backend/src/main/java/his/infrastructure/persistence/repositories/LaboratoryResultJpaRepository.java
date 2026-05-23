package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.LaboratoryResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaboratoryResultJpaRepository extends JpaRepository<LaboratoryResultJpaEntity, Long> {
    Optional<LaboratoryResultJpaEntity> findTopByOrdenLaboratorioOrdenLaboratorioIdOrderByCreatedAtDesc(Long ordenLaboratorioId);
}

