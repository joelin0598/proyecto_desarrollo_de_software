package his.application;


import his.adapters.exception.CustomAuthenticationException;
import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.dto.AuthResponse;
import his.application.dto.AuthenticationRequest;
import his.application.dto.RegisterPacienteRequest;
import his.application.dto.RegisterPersonalRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UserResponse;
import his.domain.Role;
import his.domain.UserEntity;
import his.domain.model.Paciente;
import his.domain.model.PersonalHospitalario;
import his.domain.ports.AuthUseCase;
import his.domain.ports.PacienteRepository;
import his.domain.ports.PersonalHospitalarioRepository;
import his.domain.ports.RegisterPacienteUseCase;
import his.domain.ports.RegisterPersonalUseCase;
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

import java.util.regex.Pattern;

/**
 * Servicio responsable del registro, autenticación y generación del token JWT.
 * Implementa los casos de uso de autenticación y registro conforme a la arquitectura hexagonal.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validaciones de formato de contraseña.</li>
 *   <li>Gestión de entidades usuario_sistema, paciente y personal_hospitalario.</li>
 *   <li>Generación de tokens JWT seguros.</li>
 *   <li>Aplicación de RN11: unicidad de email y DPI.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase, RegisterPacienteUseCase, RegisterPersonalUseCase {

    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalHospitalarioRepository personalHospitalarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()\\-+=.,])(?=\\S+$).{6,}$");

    /**
     * Valida el formato de la contraseña.
     */
    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordFormatException(
                    "Formato de contraseña inválido: mínimo 6 caracteres, una mayúscula, un número y un símbolo especial."
            );
        }
    }

    /**
     * Registro de usuario con rol USER (retrocompatibilidad).
     * Crea un registro en usuario_sistema y un perfil de paciente sin DPI.
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        validatePassword(request.getPassword());

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PACIENTE)
                .build();

        user = userRepository.save(user);

        // Crear perfil de paciente en tabla 'paciente' (sin DPI por ahora)
        Paciente paciente = Paciente.builder()
                .usuarioId(user.getUserId())
                .build();
        pacienteRepository.save(paciente);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Registro de usuario con rol ADMIN (retrocompatibilidad).
     * Crea un registro en usuario_sistema y un perfil de personal_hospitalario.
     */
    @Override
    @Transactional
    public AuthResponse registerAdmin(RegisterRequestAdmin requestAdmin) {
        if (userRepository.findUserByEmail(requestAdmin.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        // RN11: unicidad de DPI
        if (personalHospitalarioRepository.findByDpi(requestAdmin.getDpi()).isPresent()) {
            throw new DuplicateEmailException("El DPI ya está registrado en el sistema");
        }

        validatePassword(requestAdmin.getPassword());

        UserEntity user = UserEntity.builder()
                .firstName(requestAdmin.getFirstName())
                .lastName(requestAdmin.getLastName())
                .email(requestAdmin.getEmail())
                .password(passwordEncoder.encode(requestAdmin.getPassword()))
                .role(Role.ADMIN)
                .build();

        user = userRepository.save(user);

        PersonalHospitalario personal = PersonalHospitalario.builder()
                .usuarioId(user.getUserId())
                .dpi(requestAdmin.getDpi())
                .direccion(requestAdmin.getDireccion())
                .rol(Role.ADMIN)
                .build();
        personalHospitalarioRepository.save(personal);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Registro de un nuevo paciente externo (RN01, RN11).
     * Crea un registro en usuario_sistema con rol PACIENTE y un perfil en la tabla 'paciente'.
     */
    @Override
    @Transactional
    public AuthResponse registerPaciente(RegisterPacienteRequest request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        // RN11: unicidad de DPI del paciente
        if (pacienteRepository.findByDpi(request.getDpi()).isPresent()) {
            throw new DuplicateEmailException("El DPI ya está registrado en el sistema");
        }

        validatePassword(request.getPassword());

        // Validar DPI en dominio
        Paciente pacienteDominio = Paciente.builder().dpi(request.getDpi()).build();
        pacienteDominio.validarDpi();

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PACIENTE)
                .build();

        user = userRepository.save(user);

        Paciente paciente = Paciente.builder()
                .usuarioId(user.getUserId())
                .dpi(request.getDpi())
                .build();
        pacienteRepository.save(paciente);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Registro de personal hospitalario por un Administrador (RN01, RN11).
     * Crea un registro en usuario_sistema con el rol indicado y un perfil en 'personal_hospitalario'.
     */
    @Override
    @Transactional
    public AuthResponse registerPersonal(RegisterPersonalRequest request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        // RN11: unicidad de DPI del personal
        if (personalHospitalarioRepository.findByDpi(request.getDpi()).isPresent()) {
            throw new DuplicateEmailException("El DPI ya está registrado en el sistema");
        }

        validatePassword(request.getPassword());

        // Validar en dominio
        PersonalHospitalario personalDominio = PersonalHospitalario.builder()
                .dpi(request.getDpi())
                .numeroColegiado(request.getNumeroColegiado())
                .build();
        personalDominio.validarDpi();
        personalDominio.validarNumeroColegiado();

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRol())
                .build();

        user = userRepository.save(user);

        PersonalHospitalario personal = PersonalHospitalario.builder()
                .usuarioId(user.getUserId())
                .dpi(request.getDpi())
                .direccion(request.getDireccion())
                .numeroColegiado(request.getNumeroColegiado())
                .rol(request.getRol())
                .build();
        personalHospitalarioRepository.save(personal);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Autenticación de usuario existente.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthenticationRequest request) {
        try {
            var user = userRepository.findUserByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomAuthenticationException("Usuario no registrado"));

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
            } catch (BadCredentialsException e) {
                throw new CustomAuthenticationException("Contraseña incorrecta");
            }

            var jwtToken = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(mapToUserResponse(user))
                    .build();

        } catch (UsernameNotFoundException e) {
            throw new CustomAuthenticationException("Usuario no registrado");
        } catch (CustomAuthenticationException e) {
            throw e; // Already a known auth error, propagate as-is
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado durante la autenticación: " + e.getMessage());
        }
    }

    /**
     * Convierte una entidad UserEntity a un objeto UserResponse
     * para enviar solo los datos necesarios al frontend.
     */
    private UserResponse mapToUserResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
