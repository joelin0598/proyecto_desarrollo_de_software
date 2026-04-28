package his.adapters.rest;

import his.adapters.exception.CustomAuthenticationException;
import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.AuthService;
import his.application.dto.*;
import his.infrastructure.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🔐 Controlador REST para autenticación y registro de usuarios.
 * Maneja:
 * - Registro de usuarios normales (USER)
 * - Registro de administradores (ADMIN)
 * - Autenticación y generación de tokens JWT
 * - Manejo centralizado de excepciones
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Endpoint para registro de usuario normal (USER)
     * @param request Datos del usuario a registrar (firstName, lastName, email, password)
     * @return Token JWT y datos del usuario autenticado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Intento de registro - Email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("Registro exitoso - Email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para registro de administrador (ADMIN)
     * @param requestAdmin Datos del administrador (firstName, lastName, email, password, direccion, telefono, dpi)
     * @return Token JWT y datos del administrador autenticado
     */
    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequestAdmin requestAdmin) {
        log.info("Intento de registro de administrador - Email: {}", requestAdmin.getEmail());
        AuthResponse response = authService.registerAdmin(requestAdmin);
        log.info("Registro de administrador exitoso - Email: {}", requestAdmin.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para autenticación de usuario
     * @param request Credenciales del usuario (email, password)
     * @return Token JWT y datos del usuario autenticado
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Intento de autenticación - Email: {}", request.getEmail());
        AuthResponse response = authService.authenticate(request);
        log.info("Autenticación exitosa - Email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registro de paciente externo con perfil completo.
     * Crea usuario_sistema con rol PACIENTE + perfil en tabla paciente (con DPI).
     *
     * @param request Datos del paciente (firstName, lastName, email, password, dpi)
     * @return Token JWT y datos del usuario autenticado
     */
    @PostMapping("/register/paciente")
    public ResponseEntity<AuthResponse> registerPaciente(@Valid @RequestBody RegisterPacienteRequest request) {
        log.info("Intento de registro de paciente - Email: {}", request.getEmail());
        AuthResponse response = authService.registerPaciente(request);
        log.info("Registro de paciente exitoso - Email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para registro de personal hospitalario (requiere rol ADMIN).
     * Crea usuario_sistema con el rol indicado + perfil en tabla personal_hospitalario.
     *
     * @param request Datos del personal (nombre, email, password, dpi, direccion, rol, etc.)
     * @return Token JWT y datos del personal autenticado
     */
    @PostMapping("/register/personal")
    public ResponseEntity<AuthResponse> registerPersonal(@Valid @RequestBody RegisterPersonalRequest request) {
        log.info("Intento de registro de personal hospitalario - Email: {}", request.getEmail());
        AuthResponse response = authService.registerPersonal(request);
        log.info("Registro de personal hospitalario exitoso - Email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para logout del usuario
     * Revoca el token JWT actual agregándolo a la blacklist
     * @param httpRequest Petición HTTP (para extraer el token del header Authorization)
     * @return Mensaje de éxito
     */
    @PostMapping("/logout")
    public ResponseEntity<ErrorResponse> logout(HttpServletRequest httpRequest) {
        log.info("Intento de logout");

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Intento de logout sin token válido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Token no válido o no presente"));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        tokenBlacklistService.addToBlacklist(token);

        log.info("Logout exitoso - Token revocado");
        return ResponseEntity.ok(new ErrorResponse("Sesión cerrada exitosamente"));
    }

    /**
     * Manejo de excepciones de autenticación personalizada
     */
    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(CustomAuthenticationException e) {
        log.warn("Error de autenticación: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .errorMessage(e.getMessage())
                        .build());
    }

    /**
     * Manejo de excepciones de email duplicado
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("Email duplicado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Manejo de excepciones de formato de contraseña inválido
     */
    @ExceptionHandler(InvalidPasswordFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordFormatException(InvalidPasswordFormatException e) {
        log.warn("Formato de contraseña inválido: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Manejo de excepciones de validación (campos vacíos, formato incorrecto)
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación en los campos");
        log.warn("Error de validación: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    /**
     * Manejo de excepciones genéricas no controladas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado en el servidor", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(    "Error interno del servidor: " + e.getMessage()));
    }
}