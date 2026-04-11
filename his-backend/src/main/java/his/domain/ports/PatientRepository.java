package his.domain.ports;

import his.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByDpi(String dpi);
    Optional<Patient> findByEmail(String email);
    boolean existsByDpi(String dpi);
}
