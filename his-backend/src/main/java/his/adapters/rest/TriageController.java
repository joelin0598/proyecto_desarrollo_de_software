package his.adapters.rest;

import his.application.TriageService;
import his.application.dto.ErrorResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el triaje de pacientes (CU-2).
 * Endpoint base: /api/triage
 */
@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
@Slf4j
public class TriageController {

    private final TriageService triageService;

    /**
     * Registra los signos vitales y calcula la prioridad de triaje (RN04).
     * Si la prioridad es ROJO, notifica inmediatamente (FA03 de CU-2).
     */
    @PostMapping
    public ResponseEntity<TriageResponse> recordTriage(@Valid @RequestBody TriageRequest request) {
        log.info("Registrando triaje para paciente id={}", request.getPatientId());
        TriageResponse response = triageService.recordTriage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorna el historial de triajes de un paciente.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<TriageResponse>> getTriageHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(triageService.getTriageHistory(patientId));
    }

    /**
     * Retorna la lista de espera ordenada por hora de llegada.
     */
    @GetMapping("/waiting-list")
    public ResponseEntity<List<TriageResponse>> getWaitingList() {
        return ResponseEntity.ok(triageService.getWaitingList());
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
