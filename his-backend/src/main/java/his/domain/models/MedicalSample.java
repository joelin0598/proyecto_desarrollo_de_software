package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalSample {
    private Long muestraMedicaId;
    private String tipoMuestra;
    private String tipoRecipiente;
}
