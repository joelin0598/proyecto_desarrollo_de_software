package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.InsuranceCatalogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceCatalogJpaRepository extends JpaRepository<InsuranceCatalogJpaEntity, Long> {
    List<InsuranceCatalogJpaEntity> findByIsActiveTrueOrderByNombreAseguradoraAsc();
}

