package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO para el registro o actualización de un paciente (CU-2).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    private String dpi;
    private LocalDate birthDate;
    private String gender;

    // Contacto
    private String phone;
    private String email;
    private String address;

    // Emergencia
    private String emergencyContactName;
    private String emergencyContactPhone;

    // Seguro
    private String insurancePolicyNumber;
    private String insuranceProvider;
}
