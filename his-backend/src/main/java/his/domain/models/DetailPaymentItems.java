package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailPaymentItems {

    private Long itemId;
    private Long facturaId;
    private Long citaId;
    private Long ordenLaboratorioId;
    private Long recetaMedicaDetalleId;
    private Long serviciosId;
    private String descripcion;
    private double monto;
    private boolean estadoPago;
    private String observaciones;
}
