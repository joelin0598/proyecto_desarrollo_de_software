package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CU06 — Solicitud de cierre de atención de cita médica (RN13).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloseMedicalAppointmentAttentionRequest {

    // Se completa en el controlador desde el path /{citaMedicaDetalleId}/close.
    private Long citaMedicaDetalleId;

    @NotBlank(message = "La evaluacion fisica es obligatoria")
    private String evaluacionFisica;

    @NotBlank(message = "El diagnostico es obligatorio")
    private String diagnostico;

    private String ordenLaboratorio;

    private String recetaMedica;

    private String medicacionPrescrita;

    private Boolean requiereSeguimiento;
}

