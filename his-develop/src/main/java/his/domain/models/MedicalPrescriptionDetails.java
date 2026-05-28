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
    private String medicamentoNombre;
    private int cantidad;
    private String dosis;
    private String viaAdministracion;
    private Integer frecuenciaHoras;
    private Integer duracionDias;
    private boolean despachado;
    private boolean pagoValidado;
}
