package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LaboratoryOrder {
    private Long ordenLaboratorioId;
    private Long citaMedicaDetalleId;
    private Long muestraMedicaId;
    private Long personalId;
    private Long unidadAtencionId;
    private boolean pagoValidado;
}
