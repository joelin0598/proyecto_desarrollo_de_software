package his.application.dto;

import his.domain.models.PaymentOption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LaboratoryPaymentRequest {
    private String dpiPaciente;

    @NotNull(message = "metodoPago es requerido")
    private PaymentOption metodoPago;

    private String bancoTarjeta;
    private String numeroTarjeta;
    private String fechaVencimientoTarjeta;
    private String nombreTitularTarjeta;
    private String cvc;
    private Long aseguradoraId;
    private String numeroPoliza;
}

