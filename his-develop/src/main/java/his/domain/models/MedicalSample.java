package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class MedicalSample {
    private Long muestraMedicaId;
    private Long ordenLaboratorioId;
    private String tipoMuestra;
    private String codigoEtiqueta;
    private String observacion;
    private LocalDateTime createdAt;
}


