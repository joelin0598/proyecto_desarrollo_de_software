package his.application.services;
import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UpdateHospitalStaffUserRequest;
import his.application.dto.UserMaintenanceResponse;
import his.application.usecases.UserMaintenanceUseCase;
import his.domain.models.HospitalStaff;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserMaintenanceService implements UserMaintenanceUseCase {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()\\-+=.,])(?=\\S+$).{6,}$");
    private final UserRepository userRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional(readOnly = true)
    public List<UserMaintenanceResponse> listHospitalStaffUsers() {
        List<User> users = userRepository.findAllByRoleNot(Role.PACIENTE);
        Map<Long, HospitalStaff> staffByUserId = hospitalStaffRepository.findAll().stream()
                .collect(Collectors.toMap(HospitalStaff::getUsuarioId, Function.identity(), (left, right) -> left));
        return users.stream()
                .sorted((a, b) -> Long.compare(b.getUserId(), a.getUserId()))
                .map((user) -> toResponse(user, staffByUserId.get(user.getUserId())))
                .toList();
    }
    @Override
    @Transactional
    public UserMaintenanceResponse createHospitalStaffUser(RegisterRequestAdmin request) {
        if (!request.getRol().isPersonalHospitalario()) {
            throw new IllegalArgumentException("El rol PACIENTE no es valido en mantenimiento de personal");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("El correo electronico ya esta en uso");
        }
        String numeroColegiado = resolveNumeroColegiado(request);
        if (hospitalStaffRepository.existsByNumeroColegiado(numeroColegiado)) {
            throw new DuplicateEmailException("El numero de colegiado ya esta en uso");
        }
        validatePassword(request.getPassword());
        User user = User.builder()
                .active(true)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRol())
                .build();
        user = userRepository.save(user);
        HospitalStaff staff = HospitalStaff.builder()
                .usuarioId(user.getUserId())
                .rol(request.getRol())
                .especialidadId(request.getEspecialidadId())
                .unidadAtencionId(request.getUnidadAtencionId())
                .nombreCompleto(request.getNombreCompleto())
                .direccion(request.getDireccion())
                .numeroColegiado(numeroColegiado)
                .telefonoCorporativo(request.getTelefonoCorporativo())
                .build();
        staff.validateNumeroColegiadoIfPresent();
        staff = hospitalStaffRepository.save(staff);
        return toResponse(user, staff);
    }

    @Override
    @Transactional
    public UserMaintenanceResponse updateHospitalStaffUser(Long userId, UpdateHospitalStaffUserRequest request) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario con id=" + userId));
        if (targetUser.getRole() == Role.PACIENTE) {
            throw new IllegalArgumentException("No se permite actualizar cuentas de pacientes desde este modulo");
        }

        HospitalStaff staff = hospitalStaffRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe perfil de personal para usuarioId=" + userId));

        boolean hasChanges = request.getRol() != null
                || request.getEspecialidadId() != null
                || request.getUnidadAtencionId() != null
                || request.getNombreCompleto() != null
                || request.getDireccion() != null
                || request.getTelefonoCorporativo() != null
                || request.getNumeroColegiado() != null;
        if (!hasChanges) {
            throw new IllegalArgumentException("Debe enviar al menos un campo para actualizar");
        }

        if (request.getRol() != null) {
            if (!request.getRol().isPersonalHospitalario()) {
                throw new IllegalArgumentException("El rol PACIENTE no es valido en mantenimiento de personal");
            }
            targetUser.setRole(request.getRol());
            staff.setRol(request.getRol());
        }
        if (request.getEspecialidadId() != null) {
            staff.setEspecialidadId(request.getEspecialidadId());
        }
        if (request.getUnidadAtencionId() != null) {
            staff.setUnidadAtencionId(request.getUnidadAtencionId());
        }
        if (request.getNombreCompleto() != null) {
            staff.setNombreCompleto(request.getNombreCompleto().trim());
        }
        if (request.getDireccion() != null) {
            staff.setDireccion(request.getDireccion().trim());
        }
        if (request.getTelefonoCorporativo() != null) {
            staff.setTelefonoCorporativo(request.getTelefonoCorporativo().trim());
        }
        if (request.getNumeroColegiado() != null) {
            String numeroColegiado = normalizeNumeroColegiado(request.getNumeroColegiado());
            if (numeroColegiado != null
                    && !numeroColegiado.equals(staff.getNumeroColegiado())
                    && hospitalStaffRepository.existsByNumeroColegiado(numeroColegiado)) {
                throw new DuplicateEmailException("El numero de colegiado ya esta en uso");
            }
            staff.setNumeroColegiado(numeroColegiado);
        }

        User savedUser = userRepository.save(targetUser);
        staff.validateNumeroColegiadoIfPresent();

        HospitalStaff savedStaff = hospitalStaffRepository.save(staff);
        return toResponse(savedUser, savedStaff);
    }

    @Override
    @Transactional
    public UserMaintenanceResponse suspendHospitalStaffUser(Long userId, String adminEmail) {
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el administrador autenticado"));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario con id=" + userId));
        if (targetUser.getRole() == Role.PACIENTE) {
            throw new IllegalArgumentException("No se permite suspender cuentas de pacientes desde este modulo");
        }
        if (adminUser.getUserId().equals(targetUser.getUserId())) {
            throw new IllegalArgumentException("No puedes suspender tu propia cuenta administrativa");
        }
        if (targetUser.isActive()) {
            targetUser.setActive(false);
            targetUser = userRepository.save(targetUser);
        }
        HospitalStaff staff = hospitalStaffRepository.findByUsuarioId(targetUser.getUserId()).orElse(null);
        return toResponse(targetUser, staff);
    }

    @Override
    @Transactional
    public void deleteHospitalStaffUser(Long userId, String adminEmail) {
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el administrador autenticado"));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario con id=" + userId));
        if (targetUser.getRole() == Role.PACIENTE) {
            throw new IllegalArgumentException("No se permite eliminar cuentas de pacientes desde este modulo");
        }
        if (adminUser.getUserId().equals(targetUser.getUserId())) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta administrativa");
        }
        hospitalStaffRepository.deleteByUsuarioId(userId);
        userRepository.deleteById(userId);
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordFormatException(
                    "Formato de contrasena invalido: minimo 6 caracteres, una mayuscula, un numero y un simbolo especial."
            );
        }
    }
    private String resolveNumeroColegiado(RegisterRequestAdmin request) {
        return normalizeNumeroColegiado(request.getNumeroColegiado());
    }

    private String normalizeNumeroColegiado(String numeroColegiado) {
        if (numeroColegiado == null || numeroColegiado.isBlank()) {
            return null;
        }
        return numeroColegiado.trim();
    }
    private UserMaintenanceResponse toResponse(User user, HospitalStaff staff) {
        return UserMaintenanceResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .personalId(staff != null ? staff.getPersonalId() : null)
                .nombreCompleto(staff != null ? staff.getNombreCompleto() : null)
                .numeroColegiado(staff != null ? staff.getNumeroColegiado() : null)
                .telefonoCorporativo(staff != null ? staff.getTelefonoCorporativo() : null)
                .direccion(staff != null ? staff.getDireccion() : null)
                .especialidadId(staff != null ? staff.getEspecialidadId() : null)
                .unidadAtencionId(staff != null ? staff.getUnidadAtencionId() : null)
                .build();
    }
}
