package his.domain.ports;

import his.domain.models.Patient;

import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);

    Optional<Patient> findByUsuarioId(Long usuarioId);

    Optional<Patient> findByDpi(String dpi);

    Optional<Patient> findById(Long pacienteId);

    Optional<Patient> findByEmailContacto(String emailContacto);

    boolean existsByDpi(String dpi);

    boolean existsByEmailContacto(String emailContacto);
}

