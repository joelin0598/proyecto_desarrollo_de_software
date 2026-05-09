package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalPrescriptionDetails {
    private Long recetaMedicaDetalleId;
    private Long recetaMedicaId;
    private Long medicamentoId;
    private Long unidadAtencionId;
    private Long personalId;
    private int cantidad;
    private String dosis;
    private boolean pagoValidado;
    private boolean despachado;
}
