package his.adapters.rest;

import his.application.dto.ErrorResponse;
import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.usecases.AppointmentUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PACIENTE','ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<ScheduleAppointmentResponse> scheduleAppointment(
            @Valid @RequestBody ScheduleAppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ScheduleAppointmentResponse response = appointmentUseCase.scheduleAppointment(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PACIENTE','ADMIN','DOCTOR','ENFERMERA','LABORATORISTA','FARMACEUTICO','ADMINISTRATIVO','RECEPCION')")
    public ResponseEntity<List<ScheduleAppointmentResponse>> listAppointments(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(appointmentUseCase.listAppointments(userDetails.getUsername()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException ex) {
        log.warn("Validacion de citas fallida: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validacion en la solicitud de cita");
        log.warn("Bean validation en citas fallida: {}", msg);
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado en gestion de citas", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor en gestion de citas"));
    }
}
