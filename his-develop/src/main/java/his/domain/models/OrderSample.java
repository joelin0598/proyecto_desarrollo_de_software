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
@SuppressWarnings("unused")
public class OrderSample {
    private Long ordenMuestraId;
    private Long ordenLaboratorioId;
    private Long muestraMedicaId;
    private String tipoMuestra;
    private boolean procesada;
    private LocalDateTime createdAt;
}


