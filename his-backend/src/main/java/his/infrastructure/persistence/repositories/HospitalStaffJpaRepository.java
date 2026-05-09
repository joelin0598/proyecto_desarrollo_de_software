package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalStaffJpaRepository extends JpaRepository<HospitalStaffJpaEntity, Long> {
    Optional<HospitalStaffJpaEntity> findByUsuarioSistemaUsuarioId(Long usuarioId);

    boolean existsByNumeroColejiado(String numeroColejiado);
}

