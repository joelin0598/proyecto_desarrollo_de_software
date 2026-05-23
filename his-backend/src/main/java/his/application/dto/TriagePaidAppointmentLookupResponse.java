package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriagePaidAppointmentLookupResponse {
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private Long medicoPersonalId;
    private Long especialidadId;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String estadoAdministrativo;
}

