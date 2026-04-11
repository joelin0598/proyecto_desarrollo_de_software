package his.application;


import his.adapters.exception.CustomAuthenticationException;
import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.dto.AuthResponse;
import his.application.dto.AuthenticationRequest;
import his.application.dto.PatientRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UserResponse;
import his.domain.Role;
import his.domain.UserEntity;
import his.domain.ports.AuthUseCase;
import his.domain.ports.UserRepository;
import his.infrastructure.persistence.UserGenericEntity;
import his.infrastructure.persistence.UserGenericEntityVisit;
import his.infrastructure.persistence.UserGenericRepository;
import his.infrastructure.persistence.UserGenericVisitRepository;
import his.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Servicio responsable del registro, autenticación y generación del token JWT.
 * - Aplica validaciones de formato de contraseña.
 * - Gestiona las relaciones entre entidades (User / UserGeneric / UserGenericVisit).
 * - Genera tokens JWT seguros.
 * - Devuelve respuestas completas con información del usuario autenticado.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final UserGenericRepository userGenericRepository;
    private final UserGenericVisitRepository userGenericVisitRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()\\-+=.,])(?=\\S+$).{6,}$");

    /**
     * 🔎 Valida el formato de la contraseña.
     */
    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordFormatException(
                    "Formato de contraseña inválido: mínimo 6 caracteres, una mayúscula, un número y un símbolo especial."
            );
        }
    }

    /**
     * 🧾 Registro de usuario con rol USER.
     * Transacción que garantiza que si algo falla, se revierte todo (usuario + relación).
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        validatePassword(request.getPassword());

        var user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        if (user.getUserGenericEntityVisitList() == null) {
            user.setUserGenericEntityVisitList(new ArrayList<>());
        }

        var client = UserGenericEntityVisit.builder()
                .userId(user)
                .build();

        user.getUserGenericEntityVisitList().add(client);
        userGenericVisitRepository.save(client);

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * 🧾 Registro de usuario con rol ADMIN.
     * Transacción que garantiza que si algo falla, se revierte todo (usuario + relación).
     */
    @Override
    @Transactional
    public AuthResponse registerAdmin(RegisterRequestAdmin requestAdmin) {
        if (userRepository.findUserByEmail(requestAdmin.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        validatePassword(requestAdmin.getPassword());

        var user = UserEntity.builder()
                .firstName(requestAdmin.getFirstName())
                .lastName(requestAdmin.getLastName())
                .email(requestAdmin.getEmail())
                .password(passwordEncoder.encode(requestAdmin.getPassword()))
                .role(Role.ADMIN)
                .build();

        user = userRepository.save(user);

        if (user.getUserGenericEntityList() == null) {
            user.setUserGenericEntityList(new ArrayList<>());
        }

        var employee = UserGenericEntity.builder()
                .userId(user)
                .direccion(requestAdmin.getDireccion())
                .dpi(requestAdmin.getDpi())
                .build();

        user.getUserGenericEntityList().add(employee);
        userGenericRepository.save(employee);

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * 🔐 Autenticación de usuario existente.
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
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado durante la autenticación: " + e.getMessage());
        }
    }

    /**
     * 🧭 Convierte una entidad UserEntity a un objeto UserResponse
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

    /**
     * 🏥 Registro de paciente con rol USER e información adicional (teléfono, dirección, DPI).
     * Transacción que garantiza que si algo falla, se revierte todo.
     */
    @Override
    @Transactional
    public AuthResponse registerPatient(PatientRequest request) {
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("El correo electrónico ya está en uso");
        }

        validatePassword(request.getPassword());

        var user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        if (user.getUserGenericEntityVisitList() == null) {
            user.setUserGenericEntityVisitList(new ArrayList<>());
        }

        var patient = UserGenericEntityVisit.builder()
                .userId(user)
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .dpi(request.getDpi())
                .build();

        user.getUserGenericEntityVisitList().add(patient);
        userGenericVisitRepository.save(patient);

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }
}
