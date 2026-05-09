package his.domain.ports;

import his.domain.models.HospitalStaff;

import java.util.Optional;

public interface HospitalStaffRepository {
    HospitalStaff save(HospitalStaff staff);

    Optional<HospitalStaff> findByUsuarioId(Long usuarioId);

    boolean existsByNumeroColegiado(String numeroColegiado);
}

