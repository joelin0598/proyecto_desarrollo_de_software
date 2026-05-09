package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmailPaciente(String emailPaciente);

    boolean existsByEmailPaciente(String emailPaciente);
}

