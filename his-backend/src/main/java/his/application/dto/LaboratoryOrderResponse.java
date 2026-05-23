package his.application.dto;

import his.domain.models.LaboratoryOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LaboratoryOrderResponse {
    private Long ordenLaboratorioId;
    private Long citaMedicaDetalleId;
    private String nombreExamen;
    private String tipoMuestra;
    private LaboratoryOrderStatus estado;
    private boolean pagoValidado;
    private String etiquetaId;
    private boolean alertaCritica;
    private String observacionesTecnico;
    private LocalDateTime createdAt;

    // resultado, si ya existe
    private LaboratoryResultResponse resultado;
}

