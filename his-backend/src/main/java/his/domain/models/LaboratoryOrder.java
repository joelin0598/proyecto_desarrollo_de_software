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
public class LaboratoryOrder {
    private Long ordenLaboratorioId;
    private Long citaMedicaDetalleId;
    private Long muestraMedicaId;
    private Long personalId;
    private Long unidadAtencionId;
    private String nombreExamen;
    private String tipoMuestra;
    private LaboratoryOrderStatus estado;
    private boolean pagoValidado;
    private String etiquetaId;
    private boolean alertaCritica;
    private String observacionesTecnico;
    private LocalDateTime createdAt;
}
