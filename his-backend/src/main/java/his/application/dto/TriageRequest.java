package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageRequest {
    private Long pacienteId;
    private Long personalId;
    private Long citaMedicaId;
    private int presionSistolica;
    private int presionDiastolica;
    private int frecuenciaCardiaca;
    private double temperatura;
    private int saturacionOxigeno;
    private double tallaCm;
    private double pesoKg;
}
