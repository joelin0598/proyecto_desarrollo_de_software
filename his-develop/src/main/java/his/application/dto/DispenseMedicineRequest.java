package his.application.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class DispenseMedicineRequest {
    @NotNull(message = "recetaMedicaDetalleId es requerido")
    private Long recetaMedicaDetalleId;
}
