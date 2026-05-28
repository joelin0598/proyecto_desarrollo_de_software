package his.application.dto;

import his.domain.models.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageListItemsResponse {

    private Long signosVitalesId;
    private Long pacienteId;
    private LocalDateTime fechaHoraRegistro;

    private String nombreCompleto;
    private String dpi;

    private Priority prioridad;
    private boolean alertaEmergencia;

    private int presionSistolica;
    private int presionDiastolica;
    private int frecuenciaCardiaca;
    private double temperatura;
    private int saturacionOxigeno;
    private double pesoKg;
    private double tallaCm;
}