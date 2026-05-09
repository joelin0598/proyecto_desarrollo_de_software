package his.domain.ports;

import his.domain.models.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(Long userId);

    boolean existsByEmail(String email);
}
