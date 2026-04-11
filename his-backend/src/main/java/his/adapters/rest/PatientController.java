package his.adapters.rest;

import his.application.PatientService;
import his.application.dto.ErrorResponse;
import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pacientes.
 * Maneja:
 * - Creación de nuevos pacientes
 * - Consulta de pacientes
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * Endpoint para crear un nuevo paciente
     * @param request Datos del paciente (firstName, lastName, email, phone, dateOfBirth, gender, address, dpi)
     * @return Datos del paciente creado
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Solicitud de creación de paciente - Email: {}", request.getEmail());
        PatientResponse response = patientService.createPatient(request);
        log.info("Paciente creado exitosamente - ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para obtener todos los pacientes
     * @return Lista de pacientes
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        log.info("Solicitud de listado de pacientes");
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Endpoint para obtener un paciente por ID
     * @param id ID del paciente
     * @return Datos del paciente
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        log.info("Solicitud de paciente - ID: {}", id);
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Manejo de excepciones de validación de campos
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación en los campos");
        log.warn("Error de validación en paciente: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    /**
     * Manejo de excepciones de duplicados o paciente no encontrado
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Error en datos del paciente: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Manejo de excepciones genéricas no controladas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Error inesperado al gestionar paciente", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor: " + e.getMessage()));
    }
}
