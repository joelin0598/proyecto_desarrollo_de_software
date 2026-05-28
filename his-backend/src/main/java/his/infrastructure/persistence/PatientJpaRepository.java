package his.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientJpaRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByDpi(String dpi);

    boolean existsByDpi(String dpi);
}
