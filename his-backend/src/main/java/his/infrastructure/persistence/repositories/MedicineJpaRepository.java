package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineJpaRepository extends JpaRepository<MedicineJpaEntity, Long> {
    List<MedicineJpaEntity> findByIsActiveTrueOrderByNombreAsc();
}

