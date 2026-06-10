package his.adapters.rest;

import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.application.dto.ErrorResponse;
import his.application.dto.LaboratoryOrderResponse;
import his.application.usecases.LaboratoryUseCase;
import his.application.services.PatientFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Laboratorio", description = "Gestión de exámenes de laboratorio: órdenes, muestras y resultados")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/laboratory")
@RequiredArgsConstructor
@Slf4j
public class LaboratoryController {

    private final LaboratoryUseCase useCase;
    private final PatientFlowService patientFlowService;

    @Operation(summary = "Crear orden de laboratorio desde una cita en curso")
    @PostMapping("/orders")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','DOCTOR','ADMIN')")
    public ResponseEntity<LaboratoryOrderResponse> createOrder(
            @Valid @RequestBody CreateLaboratoryOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.createOrder(req, getEmail()));
    }

    @Operation(summary = "Confirmar recepción de muestra (EN_PROCESO + etiqueta)")
    @PatchMapping("/orders/{ordenLaboratorioId}/receive")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','ADMIN')")
    public ResponseEntity<LaboratoryOrderResponse> receive(@PathVariable Long ordenLaboratorioId) {
        return ResponseEntity.ok(useCase.receiveSample(ordenLaboratorioId, getEmail()));
    }

    @Operation(summary = "Rechazar muestra (FA02)")
    @PatchMapping("/orders/{ordenLaboratorioId}/reject")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','ADMIN')")
    public ResponseEntity<LaboratoryOrderResponse> reject(
            @PathVariable Long ordenLaboratorioId,
            @RequestParam String motivo) {
        return ResponseEntity.ok(useCase.rejectSample(ordenLaboratorioId, motivo, getEmail()));
    }

    @Operation(summary = "Registrar resultado técnico y finalizar orden")
    @PostMapping("/orders/result")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','ADMIN')")
    public ResponseEntity<LaboratoryOrderResponse> addResult(
            @Valid @RequestBody AddLaboratoryResultRequest req) {
        return ResponseEntity.ok(useCase.addResult(req, getEmail()));
    }

    @Operation(summary = "Obtener órdenes de un detalle de cita")
    @GetMapping("/orders/by-detalle/{citaMedicaDetalleId}")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','DOCTOR','ADMIN')")
    public ResponseEntity<List<LaboratoryOrderResponse>> getByDetalle(@PathVariable Long citaMedicaDetalleId) {
        return ResponseEntity.ok(useCase.getOrdersByDetalle(citaMedicaDetalleId));
    }

    @Operation(summary = "Obtener una orden específica con su resultado")
    @GetMapping("/orders/{ordenLaboratorioId}")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','DOCTOR','ADMIN')")
    public ResponseEntity<LaboratoryOrderResponse> getOrder(@PathVariable Long ordenLaboratorioId) {
        return ResponseEntity.ok(useCase.getOrder(ordenLaboratorioId));
    }

    @Operation(summary = "Obtener resultados COMPLETADOS por paciente (solo COMPLETADO visible para paciente)")
    @GetMapping("/results-by-patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('LABORATORISTA','DOCTOR','ADMIN','PACIENTE')")
    public ResponseEntity<List<LaboratoryOrderResponse>> getResultsByPatient(@PathVariable Long patientId) {
        // Si el solicitante es un paciente, permitir solo acceder a su propio id
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PACIENTE"))) {
            var current = patientFlowService.getCurrentPatientData(auth.getName());
            Number id = (Number) current.get("id");
            if (id == null || id.longValue() != patientId.longValue()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(useCase.getResultsByPatient(patientId));
    }

    // ── Exception handlers ────────────────────────────────────────────────────

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        log.warn("Validacion laboratorio: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBean(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Error de validación");
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en laboratorio", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno en laboratorio"));
    }

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Sin usuario autenticado");
        }
        return auth.getName();
    }
}

