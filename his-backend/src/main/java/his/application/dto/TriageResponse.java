package his.application.dto;

import his.domain.TriagePriority;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para el registro de triaje.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageResponse {

    private Long triageId;
    private PatientResponse patient;
    private Double systolicPressure;
    private Double diastolicPressure;
    private Double heartRate;
    private Double temperature;
    private Double oxygenSaturation;
    private Double weight;
    private TriagePriority priority;
    private String notes;
    private LocalDateTime arrivalTime;
    private String registeredBy;
}
