package his.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para agendar una cita médica (CU-0).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long patientId;

    private String doctorName;
    private String specialty;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDateTime appointmentDate;

    private String notes;
}
