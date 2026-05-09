package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCatalog {

    private Long servicioId;
    private String nombreServicio;
    private String categoria;
    private double precio;
}
