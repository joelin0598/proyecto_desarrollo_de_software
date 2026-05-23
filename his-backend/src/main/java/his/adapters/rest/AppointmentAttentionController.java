package his.adapters.rest;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.ErrorResponse;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;
import his.application.usecases.MedicalAppointmentAttentionUseCase;
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

@Tag(name = "Atencion de Citas (CU06)", description = "Atencion medica: cola de espera, apertura y cierre de atencion sobre citas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/appointments/attention")
@RequiredArgsConstructor
@Slf4j
public class AppointmentAttentionController {

    private final MedicalAppointmentAttentionUseCase useCase;

    @Operation(summary = "Cola de espera del medico autenticado (RN09)")
    @GetMapping("/queue")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<List<MedicalAppointmentQueueItemResponse>> getQueue() {
        return ResponseEntity.ok(useCase.getPatientQueue(getAuthenticatedEmail()));
    }

    @Operation(summary = "Atencion EN_CURSO del medico autenticado")
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<MedicalAppointmentAttentionResponse> getCurrent() {
        MedicalAppointmentAttentionResponse resp = useCase.getCurrentAttention(getAuthenticatedEmail());
        if (resp == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Iniciar atencion para una cita PROGRAMADA (FA01)")
    @PostMapping("/open")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<MedicalAppointmentAttentionResponse> open(@RequestParam Long citaMedicaId) {
        return ResponseEntity.ok(useCase.openAttention(citaMedicaId, getAuthenticatedEmail()));
    }

    @Operation(summary = "Cancelar atencion en curso sin finalizar consulta")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<Void> cancelCurrent() {
        boolean cancelled = useCase.cancelCurrentAttention(getAuthenticatedEmail());
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Cerrar atencion con registro clinico completo (RN13, FA03)")
    @PatchMapping("/{citaMedicaDetalleId}/close")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<MedicalAppointmentAttentionResponse> close(
            @PathVariable Long citaMedicaDetalleId,
            @Valid @RequestBody CloseMedicalAppointmentAttentionRequest request
    ) {
        request.setCitaMedicaDetalleId(citaMedicaDetalleId);
        return ResponseEntity.ok(useCase.closeAttention(request, getAuthenticatedEmail()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        log.warn("Validacion de atencion de cita fallida: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion en la solicitud de atencion de cita");
        log.warn("Bean validation de atencion de cita fallida: {}", msg);
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en atencion de cita", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor en atencion de cita"));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("No existe un usuario autenticado para la atencion de cita");
        }
        return authentication.getName();
    }
}

