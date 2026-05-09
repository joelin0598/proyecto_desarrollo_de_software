package his.infrastructure.persistence.adapter;

import his.domain.models.User;
import his.domain.ports.UserRepository;
import his.infrastructure.persistence.repositories.UserJpaRepository;
import his.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmailPaciente(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var saved = userJpaRepository.save(UserMapper.toJpa(user));
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmailPaciente(email);
    }
}

