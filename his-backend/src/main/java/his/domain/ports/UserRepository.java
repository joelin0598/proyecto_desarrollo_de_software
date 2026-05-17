package his.domain.ports;

import his.domain.models.User;
import his.domain.models.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(Long userId);

    List<User> findAllByRoleNot(Role role);

    boolean existsByEmail(String email);

    void deleteById(Long userId);
}
