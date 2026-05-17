package his.adapters.rest;
import his.application.dto.ErrorResponse;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UpdateHospitalStaffUserRequest;
import his.application.dto.UserMaintenanceResponse;
import his.application.usecases.UserMaintenanceUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/users/maintenance")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('ADMIN','ENFERMERA')")
public class UserMaintenanceController {
    private final UserMaintenanceUseCase userMaintenanceUseCase;
    @GetMapping
    public ResponseEntity<List<UserMaintenanceResponse>> listHospitalStaffUsers() {
        return ResponseEntity.ok(userMaintenanceUseCase.listHospitalStaffUsers());
    }
    @PostMapping("/staff")
    public ResponseEntity<UserMaintenanceResponse> createHospitalStaffUser(@Valid @RequestBody RegisterRequestAdmin request) {
        UserMaintenanceResponse response = userMaintenanceUseCase.createHospitalStaffUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<UserMaintenanceResponse> suspendHospitalStaffUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserMaintenanceResponse response = userMaintenanceUseCase.suspendHospitalStaffUser(userId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserMaintenanceResponse> updateHospitalStaffUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateHospitalStaffUserRequest request) {
        UserMaintenanceResponse response = userMaintenanceUseCase.updateHospitalStaffUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteHospitalStaffUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        userMaintenanceUseCase.deleteHospitalStaffUser(userId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
        log.warn("Validacion de mantenimiento de usuarios fallida: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion en la solicitud de mantenimiento");
        log.warn("Bean validation en mantenimiento de usuarios fallida: {}", msg);
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en mantenimiento de usuarios", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor en mantenimiento de usuarios"));
    }
}
