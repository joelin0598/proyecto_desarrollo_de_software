package his.adapters.rest;

import his.application.PaymentService;
import his.application.dto.AppointmentResponse;
import his.application.dto.ErrorResponse;
import his.application.dto.PaymentRequest;
import his.application.dto.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la verificación de pago y filtro de atención (CU-05).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/payments/pending – Lista de pacientes pendientes de pago (ADMIN)</li>
 *   <li>POST /api/payments – Registrar transacción de cobro (ADMIN)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Retorna la lista de citas con estado PENDING_PAYMENT para el recepcionista.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getPendingPayments() {
        log.info("Consultando pacientes pendientes de pago");
        return ResponseEntity.ok(paymentService.getPatientsAwaitingPayment());
    }

    /**
     * Registra la transacción de cobro y actualiza el estado administrativo del paciente.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Registrando pago para cita {} - Método: {}", request.getAppointmentId(), request.getPaymentMethod());
        PaymentResponse response = paymentService.registerPayment(request);
        log.info("Pago {} registrado con estado: {}", response.getId(), response.getPaymentStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Error en PaymentController: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error procesando el pago: " + e.getMessage()));
    }
}
