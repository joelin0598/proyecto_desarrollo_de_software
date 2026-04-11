package his.adapters.rest;

import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.AuthService;
import his.application.dto.AuthResponse;
import his.application.dto.ErrorResponse;
import his.application.dto.PatientRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🏥 Controlador REST para registro de pacientes.
 * Requiere autenticación JWT válida (anyRequest().authenticated() en SecurityConfig).
 * Solo accesible desde la ruta /create-patient protegida para el rol ADMIN en el frontend.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final AuthService authService;

    /**
     * Endpoint para registro de un nuevo paciente.
     * @param request Datos del paciente (firstName, lastName, email, password, telefono, direccion, dpi)
     * @return Token JWT y datos del usuario creado
     */
    @PostMapping
    public ResponseEntity<AuthResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Creando paciente - Email: {}", request.getEmail());
        AuthResponse response = authService.registerPatient(request);
        log.info("Paciente creado exitosamente - Email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("Email duplicado al crear paciente: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordFormatException(InvalidPasswordFormatException e) {
        log.warn("Formato de contraseña inválido al crear paciente: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación en los campos");
        log.warn("Error de validación al crear paciente: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado al crear paciente", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor: " + e.getMessage()));
    }
}
