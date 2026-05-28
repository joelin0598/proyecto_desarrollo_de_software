package his.infrastructure.persistence.adapter;
import his.domain.models.HospitalStaff;
import his.domain.models.Role;
import his.domain.ports.HospitalStaffRepository;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import his.infrastructure.persistence.repositories.MedicalSpecialityJpaRepository;
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
    private final MedicalSpecialityJpaRepository especialidadJpaRepository;
    @Override
    public HospitalStaff save(HospitalStaff staff) {
        MedicalSpecialityCatalogJpaEntity especialidad = null;
        if (staff.getEspecialidadId() != null) {
            especialidad = especialidadJpaRepository.findById(staff.getEspecialidadId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe especialidad_medica para especialidadId=" + staff.getEspecialidadId()));
        }
        var usuario = userJpaRepository.findById(staff.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario_sistema para usuarioId=" + staff.getUsuarioId()));
        var saved = hospitalStaffJpaRepository.save(HospitalStaffMapper.toEntity(staff, usuario, especialidad));
        return HospitalStaffMapper.toDomain(saved);
    }
    @Override
    public Optional<HospitalStaff> findByUsuarioId(Long usuarioId) {
        return hospitalStaffJpaRepository.findByUsuarioSistemaUsuarioId(usuarioId)
                .map(HospitalStaffMapper::toDomain);
    }
    @Override
    public Optional<HospitalStaff> findById(Long personalId) {
        return hospitalStaffJpaRepository.findById(personalId)
                .map(HospitalStaffMapper::toDomain);
    }
    @Override
    public List<HospitalStaff> findAll() {
        return hospitalStaffJpaRepository.findAll().stream()
                .map(HospitalStaffMapper::toDomain)
                .toList();
    }
    @Override
    public List<HospitalStaff> findAllDoctors() {
        return hospitalStaffJpaRepository.findByRolAndIsActiveTrue(Role.DOCTOR).stream()
                .map(HospitalStaffMapper::toDomain)
                .toList();
    }
    @Override
    public List<HospitalStaff> findDoctorsByEspecialidadId(Long especialidadId) {
        return hospitalStaffJpaRepository.findByEspecialidadEspecialidadIdAndRolAndIsActiveTrue(especialidadId, Role.DOCTOR).stream()
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