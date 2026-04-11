package his.adapters.rest;

import his.application.PatientService;
import his.application.dto.ErrorResponse;
import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de pacientes (CU-2).
 * Endpoint base: /api/patients
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * Registra un nuevo paciente (FA01 de CU-2: Paciente no registrado / Primera visita).
     */
    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Registro de nuevo paciente: {}", request.getFullName());
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza los datos de un paciente existente.
     */
    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long patientId,
            @Valid @RequestBody PatientRequest request) {
        log.info("Actualización de paciente id={}", patientId);
        PatientResponse response = patientService.updatePatient(patientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los datos de un paciente por su ID.
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId) {
        PatientResponse response = patientService.getPatientById(patientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca un paciente por su DPI/CUI.
     */
    @GetMapping("/search")
    public ResponseEntity<?> findByDpi(@RequestParam String dpi) {
        Optional<PatientResponse> result = patientService.findByDpi(dpi);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("No se encontró paciente con DPI: " + dpi));
    }

    /**
     * Lista todos los pacientes registrados.
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Error de validación");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(msg));
    }
}
