package his.infrastructure.persistence.adapter;

import his.domain.models.HospitalStaff;
import his.domain.ports.HospitalStaffRepository;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.UserJpaRepository;
import his.infrastructure.persistence.mapper.HospitalStaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlHospitalStaffRepository implements HospitalStaffRepository {

    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public HospitalStaff save(HospitalStaff staff) {
        var usuario = userJpaRepository.findById(staff.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario_sistema para usuarioId=" + staff.getUsuarioId()));

        var saved = hospitalStaffJpaRepository.save(HospitalStaffMapper.toEntity(staff, usuario));
        return HospitalStaffMapper.toDomain(saved);
    }

    @Override
    public Optional<HospitalStaff> findByUsuarioId(Long usuarioId) {
        return hospitalStaffJpaRepository.findByUsuarioSistemaUsuarioId(usuarioId)
                .map(HospitalStaffMapper::toDomain);
    }

    @Override
    public List<HospitalStaff> findAll() {
        return hospitalStaffJpaRepository.findAll().stream()
                .map(HospitalStaffMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNumeroColegiado(String numeroColegiado) {
        if (numeroColegiado == null || numeroColegiado.isBlank()) {
            return false;
        }
        return hospitalStaffJpaRepository.existsByNumeroColejiado(numeroColegiado);
    }

    @Override
    public void deleteByUsuarioId(Long usuarioId) {
        hospitalStaffJpaRepository.deleteByUsuarioSistemaUsuarioId(usuarioId);
    }
}

