package his.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link PacienteJpaEntity} (tabla: paciente).
 * Utilizado por el adaptador {@link his.infrastructure.persistence.adapters.SqlPacienteRepositoryAdapter}.
 */
public interface PacienteJpaRepository extends JpaRepository<PacienteJpaEntity, Long> {

    Optional<PacienteJpaEntity> findByDpi(String dpi);

    Optional<PacienteJpaEntity> findByUsuarioUserId(Long usuarioId);
}
