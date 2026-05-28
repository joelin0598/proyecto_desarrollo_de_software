package his.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientRegisterResponse {
    private Long pacienteId;
    private Long citaMedicaId;
    private boolean pacienteNuevo;
    private boolean pagoValidado;
    private String mensaje;
}

