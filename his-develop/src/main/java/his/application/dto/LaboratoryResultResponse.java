package his.application.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class LaboratoryResultResponse {
    private Long resultadoLaboratorioId;
    private Long ordenLaboratorioId;
    private String nombreExamen;
    private BigDecimal valorResultado;
    private String unidadResultado;
    private BigDecimal referenciaMinima;
    private BigDecimal referenciaMaxima;
    private String observaciones;
    private String resumen;
    private String conclusion;
    private boolean critico;
    private LocalDateTime createdAt;
}
