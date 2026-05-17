package his.infrastructure.persistence.repositories;

import his.domain.models.Role;
import his.infrastructure.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmailPaciente(String emailPaciente);

    List<UserJpaEntity> findAllByRolNot(Role rol);

    boolean existsByEmailPaciente(String emailPaciente);
}

