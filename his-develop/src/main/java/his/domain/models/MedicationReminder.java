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
public class MedicationReminder {
    private Long recordatorioId;
    private Long recetaMedicaDetalleId;
    private Long pacienteId;
    private String medicamentoNombre;
    private String dosis;
    private Integer frecuenciaHoras;
    private Integer duracionDias;
    private String viaAdministracion;
    private LocalDateTime proximoRecordatorio;
    private boolean activo;
}

