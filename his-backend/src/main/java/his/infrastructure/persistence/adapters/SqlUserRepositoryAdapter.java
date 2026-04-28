package his.infrastructure.persistence.adapters;

import his.domain.UserEntity;
import his.domain.ports.UserRepository;
import his.infrastructure.persistence.UsuarioSistemaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto {@link UserRepository}.
 * Traduce las operaciones de dominio en operaciones JPA sobre la tabla 'usuario_sistema'.
 */
@Repository
@RequiredArgsConstructor
public class SqlUserRepositoryAdapter implements UserRepository {

    private final UsuarioSistemaJpaRepository jpaRepository;

    @Override
    public Optional<UserEntity> findUserByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return jpaRepository.save(user);
    }
}
