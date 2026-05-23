package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointmentDetails {
    private Long medicalAppointmentDetailsId;
    private Long citaMedicaId;
    private String evaluacionFisica;
    private String diagnostico;
    private String ordenLaboratorio;
    private String recetaMedica;
    private String medicacionPrescrita;
    private Boolean requiereSeguimiento;
    private Long citaSeguimientoId;
    private LocalDateTime createdAt;
}
