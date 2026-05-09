package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceCatalog {

    private Long aseguradoraId;
    private String nombre;
    private String descripcion;
    private String polizaSeguro;
}
