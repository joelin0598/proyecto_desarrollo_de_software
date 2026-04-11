package his.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para registrar el triaje de un paciente (CU-2).
 * Incluye signos vitales necesarios para calcular la prioridad (RN04).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long patientId;

    // Signos vitales
    @DecimalMin(value = "0.0", message = "La presión sistólica no puede ser negativa")
    @DecimalMax(value = "300.0", message = "La presión sistólica excede el rango válido")
    private Double systolicPressure;

    @DecimalMin(value = "0.0", message = "La presión diastólica no puede ser negativa")
    @DecimalMax(value = "200.0", message = "La presión diastólica excede el rango válido")
    private Double diastolicPressure;

    @DecimalMin(value = "0.0", message = "La frecuencia cardíaca no puede ser negativa")
    @DecimalMax(value = "300.0", message = "La frecuencia cardíaca excede el rango válido")
    private Double heartRate;

    @DecimalMin(value = "25.0", message = "La temperatura es físicamente imposible")
    @DecimalMax(value = "45.0", message = "La temperatura excede el rango válido")
    private Double temperature;

    @DecimalMin(value = "0.0", message = "La saturación de oxígeno no puede ser negativa")
    @DecimalMax(value = "100.0", message = "La saturación de oxígeno no puede superar el 100%")
    private Double oxygenSaturation;

    @DecimalMin(value = "0.0", message = "El peso no puede ser negativo")
    @DecimalMax(value = "500.0", message = "El peso excede el rango válido")
    private Double weight;

    private String notes;
}
