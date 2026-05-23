package his.infrastructure.persistence.repositories;

import his.domain.models.Role;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalStaffJpaRepository extends JpaRepository<HospitalStaffJpaEntity, Long> {
    Optional<HospitalStaffJpaEntity> findByUsuarioSistemaUsuarioId(Long usuarioId);

    boolean existsByNumeroColejiado(String numeroColejiado);

    void deleteByUsuarioSistemaUsuarioId(Long usuarioId);

    List<HospitalStaffJpaEntity> findByRolAndIsActiveTrue(Role rol);

    List<HospitalStaffJpaEntity> findByEspecialidadEspecialidadIdAndRolAndIsActiveTrue(Long especialidadId, Role rol);
}
