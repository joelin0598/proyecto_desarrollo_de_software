package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalSpecialityCatalog {

    private Long especialidadMedicaId;
    private String nombre;
    private String descripcion;
}
