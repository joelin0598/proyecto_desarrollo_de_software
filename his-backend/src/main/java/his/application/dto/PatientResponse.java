package his.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos del paciente registrado.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponse {

    private Long patientId;
    private String fullName;
    private String dpi;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insurancePolicyNumber;
    private String insuranceProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
