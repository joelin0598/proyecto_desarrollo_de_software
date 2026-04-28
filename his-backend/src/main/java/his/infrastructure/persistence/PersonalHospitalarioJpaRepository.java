package his.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link PersonalHospitalarioJpaEntity} (tabla: personal_hospitalario).
 * Utilizado por el adaptador {@link his.infrastructure.persistence.adapters.SqlPersonalHospitalarioRepositoryAdapter}.
 */
public interface PersonalHospitalarioJpaRepository extends JpaRepository<PersonalHospitalarioJpaEntity, Long> {

    Optional<PersonalHospitalarioJpaEntity> findByDpi(String dpi);

    Optional<PersonalHospitalarioJpaEntity> findByUsuarioUserId(Long usuarioId);
}
