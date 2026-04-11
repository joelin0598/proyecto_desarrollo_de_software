package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la solicitud de programación de cita (CU-04).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    @NotBlank(message = "El nombre del médico es obligatorio")
    private String doctorName;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDate appointmentDate;

    @NotBlank(message = "El horario es obligatorio")
    private String appointmentTime;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    private String reason;

    // Datos de cobertura de seguro (opcionales)
    private String insurerName;
    private String policyNumber;
    private String holderDpi;
}
