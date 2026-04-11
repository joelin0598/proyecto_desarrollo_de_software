package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el registro de una transacción de cobro (CU-05).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long appointmentId;

    @NotBlank(message = "El método de pago es obligatorio")
    private String paymentMethod;

    private String authorizationNumber;

    @NotNull(message = "El monto total es obligatorio")
    private Double totalAmount;

    private Double insuranceCoverage;

    private Double pendingBalance;

    @NotBlank(message = "El número de factura es obligatorio")
    private String invoiceNumber;

    // Indicador de emergencia crítica (Código Rojo - FA02)
    private Boolean emergencyBypass;
}
