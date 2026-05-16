package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.VitalSignsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;   

import java.util.List;

@Repository
public interface VitalSignsJpaRepository extends JpaRepository<VitalSignsJpaEntity, Long> {
    List<VitalSignsJpaEntity> findAllByOrderByCreatedAtDesc();
}
