package his.application.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class PrescriptionResponse {
    private Long recetaMedicaId;
    private Long citaMedicaDetalleId;
    private String instruccionesGenerales;
    private LocalDate fechaEmision;
    private LocalDateTime createdAt;
    private List<PrescriptionDetailResponse> items;
    @Data
    @Builder
    public static class PrescriptionDetailResponse {
        private Long recetaMedicaDetalleId;
        private Long medicamentoId;
        private String medicamentoNombre;
        private Integer cantidad;
        private String dosis;
        private String viaAdministracion;
        private Integer frecuenciaHoras;
        private Integer duracionDias;
        private boolean despachado;
        private boolean pagoValidado;
    }
}
