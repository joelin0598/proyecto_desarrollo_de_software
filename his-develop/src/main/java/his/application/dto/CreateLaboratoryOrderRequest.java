package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLaboratoryOrderRequest {

    @NotNull(message = "citaMedicaDetalleId es requerido")
    private Long citaMedicaDetalleId;

    @NotBlank(message = "nombreExamen es requerido")
    private String nombreExamen;

    private String tipoMuestra;
}

