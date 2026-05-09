package his.adapters.rest;

import his.application.dto.ErrorResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.application.usecases.TriageUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
@Slf4j
public class TriageController {

    private final TriageUseCase triageUseCase;

    /**
     * CU 2.0 — Registro de paciente + clasificación de urgencia.
     * El personalId se resuelve internamente desde el JWT del usuario autenticado.
     * Prioridad calculada en el dominio (RN04), nunca en el frontend.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<TriageResponse> registrarTriaje(
            @Valid @RequestBody TriageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String emailPersonal = userDetails.getUsername();
        log.info("Triaje iniciado por personal={} para DPI={}", emailPersonal, request.getDpi());

        TriageResponse response = triageUseCase.execute(request, emailPersonal);

        if (response.isAlertaEmergencia()) {
            log.warn("FA03 CODIGO ROJO — pacienteId={}, personal activó alerta de emergencia extrema",
                    response.getPacienteId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
        log.warn("Validacion de triaje fallida: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion en la solicitud de triaje");
        log.warn("Bean validation de triaje fallida: {}", msg);
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en triaje", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor al registrar el triaje"));
    }
}
