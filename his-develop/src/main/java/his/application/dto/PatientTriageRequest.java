package his.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PatientTriageRequest {
    private Long citaMedicaId;
    private String dpi;

    @Min(50) @Max(300)
    private int presionSistolica;

    @Min(30) @Max(200)
    private int presionDiastolica;

    @Min(20) @Max(250)
    private int frecuenciaCardiaca;

    @DecimalMin("30.0") @DecimalMax("45.0")
    private double temperatura;

    @Min(50) @Max(100)
    private int saturacionOxigeno;

    @DecimalMin("1.0") @DecimalMax("500.0")
    private double pesoKg;

    @DecimalMin("30.0") @DecimalMax("300.0")
    private double tallaCm;
}

