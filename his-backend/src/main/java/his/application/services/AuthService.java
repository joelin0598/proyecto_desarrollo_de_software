package his.application.services;

import his.adapters.exception.CustomAuthenticationException;
import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.dto.AuthResponse;
import his.application.dto.AuthenticationRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UserResponse;
import his.domain.models.HospitalStaff;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.User;
import his.application.usecases.AuthUseCase;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import his.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()\\-+=.,])(?=\\S+$).{6,}$");

    /**
     * Política mínima de contraseña para registros en usuario_sistema.
     */
    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordFormatException(
                    "Formato de contraseña inválido: mínimo 6 caracteres, una mayúscula, un número y un símbolo especial."
            );
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        validatePassword(request.getPassword());

        var user = User.builder()
                .active(true)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PACIENTE)
                .build();

        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .usuarioId(user.getUserId())
                .nombreCompleto(buildFullName(request.getFirstName(), request.getLastName()))
                .build();
        patient.validateDpiIfPresent();
        patient = patientRepository.save(patient);

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(mapToUserResponse(user, Optional.of(patient), Optional.empty()))
                .build();
    }

    /**
     * Registro de personal_hospitalario con vínculo a usuario_sistema.
     * En fase actual se usa rol técnico ADMIN para este flujo.
     */
    @Override
    @Transactional
    public AuthResponse registerPersonal(RegisterRequestAdmin requestAdmin) {
        if (userRepository.existsByEmail(requestAdmin.getEmail())) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        String numeroColegiado = resolveNumeroColegiado(requestAdmin);
        if (hospitalStaffRepository.existsByNumeroColegiado(numeroColegiado)) {
            throw new DuplicateEmailException("El número de colegiado ya está en uso");
        }

        validatePassword(requestAdmin.getPassword());

        var user = User.builder()
                .active(true)
                .email(requestAdmin.getEmail())
                .password(passwordEncoder.encode(requestAdmin.getPassword()))
                .role(Role.ADMIN)
                .build();

        user = userRepository.save(user);

        HospitalStaff staff = HospitalStaff.builder()
                .usuarioId(user.getUserId())
                .rol(Role.ADMIN)
                .nombreCompleto(buildFullName(requestAdmin.getFirstName(), requestAdmin.getLastName()))
                .direccion(requestAdmin.getDireccion())
                .numeroColegiado(numeroColegiado)
                .telefonoCorporativo(requestAdmin.getTelefono())
                .build();
        staff.validateNumeroColegiadoIfPresent();
        staff = hospitalStaffRepository.save(staff);

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(mapToUserResponse(user, Optional.empty(), Optional.of(staff)))
                .build();
    }

    @Override
    public AuthResponse registerAdmin(RegisterRequestAdmin requestAdmin) {
        return registerPersonal(requestAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthenticationRequest request) {
        try {
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomAuthenticationException("Usuario no registrado"));

            // RN01: no se autentican cuentas suspendidas/inactivas.
            if (!user.isActive()) {
                throw new CustomAuthenticationException("Cuenta suspendida o inactiva");
            }

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );
            } catch (BadCredentialsException e) {
                throw new CustomAuthenticationException("Contraseña incorrecta");
            }

            Optional<Patient> patient = Optional.empty();
            Optional<HospitalStaff> staff = Optional.empty();

            if (user.getRole() == Role.PACIENTE) {
                patient = patientRepository.findByUsuarioId(user.getUserId());
            } else if (user.getRole().isPersonalHospitalario()) {
                staff = hospitalStaffRepository.findByUsuarioId(user.getUserId());
            }

            return AuthResponse.builder()
                    .token(jwtService.generateToken(user))
                    .user(mapToUserResponse(user, patient, staff))
                    .build();

        } catch (UsernameNotFoundException e) {
            throw new CustomAuthenticationException("Usuario no registrado");
        }
    }

    private UserResponse mapToUserResponse(User user, Optional<Patient> patient, Optional<HospitalStaff> staff) {
        String[] names = resolveNames(patient, staff);
        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .firstName(names[0])
                .lastName(names[1])
                .role(user.getRole())
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        return (firstName + " " + lastName).trim();
    }

    private String[] resolveNames(Optional<Patient> patient, Optional<HospitalStaff> staff) {
        String fullName = patient.map(Patient::getNombreCompleto)
                .or(() -> staff.map(HospitalStaff::getNombreCompleto))
                .orElse("");

        if (fullName.isBlank()) {
            return new String[]{"", ""};
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        return new String[]{firstName, lastName};
    }

    private String resolveNumeroColegiado(RegisterRequestAdmin requestAdmin) {
        if (requestAdmin.getNumeroColegiado() != null && !requestAdmin.getNumeroColegiado().isBlank()) {
            return requestAdmin.getNumeroColegiado();
        }
        return requestAdmin.getDpi();
    }
}

