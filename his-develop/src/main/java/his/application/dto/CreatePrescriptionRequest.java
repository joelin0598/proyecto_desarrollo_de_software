package his.application.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
@Data
public class CreatePrescriptionRequest {
    @NotNull(message = "citaMedicaDetalleId es requerido")
    private Long citaMedicaDetalleId;
    private String instruccionesGenerales;
    @NotEmpty(message = "Debe incluir al menos un medicamento")
    private List<PrescriptionItemRequest> items;
    @Data
    public static class PrescriptionItemRequest {
        @NotNull
        private Long medicamentoId;
        @NotNull
        private Integer cantidad;
        private String dosis;
        private String viaAdministracion;
        private Integer frecuenciaHoras;
        private Integer duracionDias;
    }
}
