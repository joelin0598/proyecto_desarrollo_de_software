package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para operaciones de pago (CU-05).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private String specialty;
    private String doctorName;
    private String paymentMethod;
    private String authorizationNumber;
    private Double totalAmount;
    private Double insuranceCoverage;
    private Double pendingBalance;
    private String invoiceNumber;
    private String paymentStatus;
    private String appointmentStatus;
    private Boolean emergencyBypass;
    private LocalDateTime createdAt;
    private String auditNote;
    private String message;
}
