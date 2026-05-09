package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistory {
    private Long historiaMedicaId;
    private Long citaMedicaId;
    private Long pacienteId;
    private String diagnostico;
    private String planTratamiento;
    private String observaciones;
}
