package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.PatientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, Long> {
    Optional<PatientJpaEntity> findByUsuarioSistemaUsuarioId(Long usuarioId);

    Optional<PatientJpaEntity> findByDpi(String dpi);

    Optional<PatientJpaEntity> findByEmailContactoIgnoreCase(String emailContacto);

    boolean existsByDpi(String dpi);

    boolean existsByEmailContactoIgnoreCase(String emailContacto);
}

