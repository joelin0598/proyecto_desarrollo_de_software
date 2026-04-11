package his.adapters.rest;

import his.application.AppointmentService;
import his.application.dto.AppointmentRequest;
import his.application.dto.AppointmentResponse;
import his.application.dto.ErrorResponse;
import his.domain.UserEntity;
import his.domain.ports.UserRepository;
import his.infrastructure.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de citas médicas (CU-04).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/appointments – Programar nueva cita (USER)</li>
 *   <li>GET  /api/appointments/my – Citas del paciente autenticado (USER)</li>
 *   <li>GET  /api/appointments/pending-payment – Citas pendientes de pago (ADMIN)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    /**
     * Programa una nueva cita médica para el paciente autenticado.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> scheduleAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        Long patientId = getCurrentUserId();
        log.info("Solicitud de cita para paciente {} - Especialidad: {}", patientId, request.getSpecialty());
        AppointmentResponse response = appointmentService.scheduleAppointment(request, patientId);
        log.info("Cita {} creada con estado: {}", response.getId(), response.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorna las citas del paciente autenticado.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        Long patientId = getCurrentUserId();
        log.info("Consultando citas para paciente {}", patientId);
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId));
    }

    /**
     * Retorna todas las citas con estado PENDING_PAYMENT (uso exclusivo ADMIN).
     */
    @GetMapping("/pending-payment")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getPendingPaymentAppointments() {
        log.info("Consultando citas pendientes de pago");
        return ResponseEntity.ok(appointmentService.getPendingPaymentAppointments());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Error en AppointmentController: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error procesando la solicitud de cita: " + e.getMessage()));
    }

    private Long getCurrentUserId() {
        String email = SecurityUtil.getCurrentUser();
        UserEntity user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado: " + email));
        return user.getUserId();
    }
}
