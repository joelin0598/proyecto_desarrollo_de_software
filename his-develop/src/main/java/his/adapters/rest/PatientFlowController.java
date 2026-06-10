package his.adapters.rest;

import his.application.dto.ErrorResponse;
import his.application.dto.PatientAvailabilityResponse;
import his.application.dto.PatientLookupResponse;
import his.application.dto.MedicalRecordResponse;
import his.application.dto.PatientRegisterRequest;
import his.application.dto.PatientRegisterResponse;
import his.application.dto.PatientTriageRequest;
import his.application.dto.TriageResponse;
import his.application.dto.UpdatePatientProfileRequest;
import his.application.services.PatientFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientFlowController {

    private final PatientFlowService service;

    @GetMapping("/availability")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<PatientAvailabilityResponse> checkAvailability(
            @RequestParam String dpi,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(service.checkAvailability(dpi, email));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<PatientLookupResponse> lookupPatientByDpi(@RequestParam String dpi) {
        return service.findPatientByDpi(dpi)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<PatientRegisterResponse> register(
            @Valid @RequestBody PatientRegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.register(request, userDetails.getUsername()));
    }

    @PostMapping("/triage")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<TriageResponse> triage(
            @Valid @RequestBody PatientTriageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.triage(request, userDetails.getUsername()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<Map<String, Object>> getCurrentPatient(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Obteniendo datos del paciente actual: {}", userDetails.getUsername());
        Map<String, Object> result = service.getCurrentPatientData(userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<Map<String, Object>> updateCurrentPatient(
            @RequestBody Map<String, String> updates,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Actualizando datos del paciente: {}", userDetails.getUsername());
        Map<String, Object> result = service.updateCurrentPatientData(userDetails.getUsername(), updates);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/profile/edit")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Actualizando perfil restringido de paciente: {}", userDetails.getUsername());
        Map<String, Object> result = service.updatePatientProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medical-record/{patientId}")
    @PreAuthorize("hasAnyAuthority('PACIENTE','DOCTOR','ADMIN','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecord(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PACIENTE"))) {
            Map<String, Object> current = service.getCurrentPatientData(userDetails.getUsername());
            Number currentPatientId = (Number) current.get("id");
            if (currentPatientId == null || currentPatientId.longValue() != patientId.longValue()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(service.getMedicalRecord(patientId));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        log.warn("Validacion de flujo de pacientes fallida: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion en flujo de pacientes");
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en flujo de pacientes", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor en flujo de pacientes"));
    }
}

