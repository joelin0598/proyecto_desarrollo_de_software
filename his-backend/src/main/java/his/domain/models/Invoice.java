package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    private Long facturaId;
    private Long pacienteId;
    private Long citaId;
    private Double totalFacturado;
    private Double montoCubiertoSeguro;
    private Boolean estadoFactura;
}
