package his.adapters.rest;

import his.application.dto.CreatePrescriptionRequest;
import his.application.dto.DispenseMedicineRequest;
import his.application.dto.ErrorResponse;
import his.application.dto.MedicationReminderResponse;
import his.application.dto.MedicineResponse;
import his.application.dto.PharmacyPaymentRequest;
import his.application.dto.PharmacyPrescriptionLookupResponse;
import his.application.dto.PrescriptionResponse;
import his.application.usecases.PharmacyUseCase;
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

@Tag(name = "Farmacia (CU08)", description = "Despacho de medicamentos y recordatorios de tratamiento")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
@Slf4j
public class PharmacyController {

    private final PharmacyUseCase useCase;

    @Operation(summary = "Listar medicamentos disponibles en inventario")
    @GetMapping("/medicines")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','DOCTOR','ADMIN')")
    public ResponseEntity<List<MedicineResponse>> listMedicines() {
        return ResponseEntity.ok(useCase.listMedicines());
    }

    @Operation(summary = "Crear receta médica (desde consulta CU06)")
    @PostMapping("/prescriptions")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.createPrescription(req, getEmail()));
    }

    @Operation(summary = "Obtener receta activa de un detalle de cita")
    @GetMapping("/prescriptions/by-detalle/{citaMedicaDetalleId}")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionResponse> getPrescription(@PathVariable Long citaMedicaDetalleId) {
        return ResponseEntity.ok(useCase.getPrescription(citaMedicaDetalleId));
    }

    @Operation(summary = "Buscar recetas activas por DPI de paciente")
    @GetMapping("/prescriptions/by-dpi")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','ADMIN')")
    public ResponseEntity<PharmacyPrescriptionLookupResponse> getPrescriptionsByDpi(@RequestParam String dpi) {
        return ResponseEntity.ok(useCase.findPrescriptionsByDpi(dpi));
    }

    @Operation(summary = "Validar pago de farmacia para receta")
    @PostMapping("/prescriptions/{recetaMedicaId}/payment")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','ADMIN')")
    public ResponseEntity<PrescriptionResponse> validatePayment(
            @PathVariable Long recetaMedicaId,
            @Valid @RequestBody PharmacyPaymentRequest req) {
        return ResponseEntity.ok(useCase.validatePrescriptionPayment(recetaMedicaId, req, getEmail()));
    }

    @Operation(summary = "Despachar receta completa (todos los items pendientes)")
    @PostMapping("/prescriptions/{recetaMedicaId}/dispense")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','ADMIN')")
    public ResponseEntity<PrescriptionResponse> dispensePrescription(@PathVariable Long recetaMedicaId) {
        return ResponseEntity.ok(useCase.dispensePrescription(recetaMedicaId, getEmail()));
    }

    @Operation(summary = "Despachar medicamento (RN09 — valida solvencia y stock)")
    @PostMapping("/dispense")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','ADMIN')")
    public ResponseEntity<PrescriptionResponse> dispense(
            @Valid @RequestBody DispenseMedicineRequest req) {
        return ResponseEntity.ok(useCase.dispense(req, getEmail()));
    }

    @Operation(summary = "Obtener recordatorios activos del paciente")
    @GetMapping("/reminders/{pacienteId}")
    @PreAuthorize("hasAnyAuthority('FARMACEUTICO','DOCTOR','ADMIN','PACIENTE')")
    public ResponseEntity<List<MedicationReminderResponse>> getReminders(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(useCase.getReminders(pacienteId));
    }

    @Operation(summary = "Obtener recordatorios activos del paciente autenticado")
    @GetMapping("/reminders/me")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<List<MedicationReminderResponse>> getMyReminders() {
        return ResponseEntity.ok(useCase.getRemindersByEmail(getEmail()));
    }

    // ── Exception handlers ────────────────────────────���───────────────────────

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        log.warn("Validacion farmacia: {}", ex.getMessage());
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
        log.error("Error inesperado en farmacia", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno en farmacia"));
    }

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Sin usuario autenticado");
        }
        return auth.getName();
    }
}

