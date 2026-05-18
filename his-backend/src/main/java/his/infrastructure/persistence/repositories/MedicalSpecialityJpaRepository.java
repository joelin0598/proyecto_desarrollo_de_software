package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalSpecialityJpaRepository extends JpaRepository<MedicalSpecialityCatalogJpaEntity, Long> {
    List<MedicalSpecialityCatalogJpaEntity> findByIsActiveTrueOrderByNombreAsc();
}
