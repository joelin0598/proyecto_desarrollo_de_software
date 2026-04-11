package his.application.dto;

import his.domain.AppointmentStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una cita médica.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long appointmentId;
    private PatientResponse patient;
    private String doctorName;
    private String specialty;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
