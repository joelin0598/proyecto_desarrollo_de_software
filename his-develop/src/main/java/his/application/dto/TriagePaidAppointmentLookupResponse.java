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
    private String fechaNacimiento;
    private String genero;
    private String telefono;
    private String emailContacto;
    private String direccion;
    private String contactoEmergencia;
    private String telefonoEmergencia;
    private Long medicoPersonalId;
    private Long especialidadId;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String estadoAdministrativo;
}

