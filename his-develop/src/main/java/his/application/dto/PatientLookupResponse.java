package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientLookupResponse {
    private Long pacienteId;
    private String nombreCompleto;
    private String dpi;
    private LocalDate fechaNacimiento;
    private String genero;
    private String telefono;
    private String emailContacto;
    private String direccion;
    private String contactoEmergencia;
    private String telefonoEmergencia;
}
