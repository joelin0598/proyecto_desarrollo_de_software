package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para operaciones de citas (CU-04).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String specialty;
    private String doctorName;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String reason;
    private String insurerName;
    private String policyNumber;
    private String status;
    private Double baseTariff;
    private Double deductible;
    private LocalDateTime createdAt;
    private String auditNote;
    private String message;
}
