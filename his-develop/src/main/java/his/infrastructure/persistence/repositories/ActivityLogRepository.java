package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.ActivityLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogJpaEntity, Long> {
}

