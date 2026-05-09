package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {
    private Long transaccionId;
    private Long facturaId;
    private Long metodoPagoId;
    private Double montoPagado;
    private Long numeroAutorizacion;
    private LocalDate fechaPago;
}
