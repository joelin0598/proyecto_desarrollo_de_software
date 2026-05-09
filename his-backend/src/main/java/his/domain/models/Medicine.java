package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Medicine {
    private Long medicamentoId;
    private String nombre;
    private String presentacion;
    private String descripcion;
    private int stockActual;
    private Double precioUnitario;
}
