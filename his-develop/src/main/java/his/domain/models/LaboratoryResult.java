package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LaboratoryResult {
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
