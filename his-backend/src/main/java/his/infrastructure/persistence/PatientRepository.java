package his.infrastructure.persistence;

import his.domain.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findByEmail(String email);
    Optional<PatientEntity> findByDpi(String dpi);
}
