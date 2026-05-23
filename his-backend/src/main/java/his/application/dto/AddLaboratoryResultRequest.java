package his.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class AddLaboratoryResultRequest {
    @NotNull(message = "ordenLaboratorioId es requerido")
    private Long ordenLaboratorioId;
    @NotBlank(message = "nombreExamen es requerido")
    private String nombreExamen;
    private BigDecimal valorResultado;
    private String unidadResultado;
    private BigDecimal referenciaMinima;
    private BigDecimal referenciaMaxima;
    private String observaciones;
    private String resumen;
    @NotBlank(message = "conclusion es requerida")
    private String conclusion;
}
