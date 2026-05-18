package his.domain.ports;
import his.domain.models.HospitalStaff;
import java.util.List;
import java.util.Optional;
public interface HospitalStaffRepository {
    HospitalStaff save(HospitalStaff staff);
    Optional<HospitalStaff> findByUsuarioId(Long usuarioId);
    Optional<HospitalStaff> findById(Long personalId);
    List<HospitalStaff> findAll();
    List<HospitalStaff> findAllDoctors();
    List<HospitalStaff> findDoctorsByEspecialidadId(Long especialidadId);
    boolean existsByNumeroColegiado(String numeroColegiado);
    void deleteByUsuarioId(Long usuarioId);
}