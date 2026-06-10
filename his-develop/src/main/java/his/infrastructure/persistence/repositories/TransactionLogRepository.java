package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.TransactionLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLogJpaEntity, Long> {
}

