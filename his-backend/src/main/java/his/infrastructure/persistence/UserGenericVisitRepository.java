package his.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenericVisitRepository extends JpaRepository<UserGenericEntityVisit, Long> {
}
