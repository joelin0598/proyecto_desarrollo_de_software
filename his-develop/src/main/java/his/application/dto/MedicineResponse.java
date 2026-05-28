package his.application.dto;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class MedicineResponse {
    private Long medicamentoId;
    private String nombre;
    private String presentacion;
    private String descripcion;
    private Integer stockActual;
    private Double precioUnitario;
}
