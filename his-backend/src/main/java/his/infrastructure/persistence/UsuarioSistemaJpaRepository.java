package his.infrastructure.persistence;

import his.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link UserEntity} (tabla: usuario_sistema).
 * Utilizado por el adaptador {@link his.infrastructure.persistence.adapters.SqlUserRepositoryAdapter}.
 */
public interface UsuarioSistemaJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
}
