package his.application.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Builder
public class MedicationReminderResponse {
    private Long recordatorioId;
    private String medicamentoNombre;
    private String dosis;
    private Integer frecuenciaHoras;
    private Integer duracionDias;
    private String viaAdministracion;
    private LocalDateTime proximoRecordatorio;
    private boolean activo;
}
