package his.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenericRepository extends JpaRepository<UserGenericEntity, Long> {
}
