package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodCatalog {
    private Long metodoPagoId;
    private Long codigoPago;
    private String nombre;
    private Boolean requiereAutorizacion;
}
