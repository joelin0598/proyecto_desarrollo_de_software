package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private Long pacienteId;
    private Long usuarioId;          // nullable: pacientes de triaje presencial no tienen cuenta web
    private Long aseguradoraId;      // nullable: seguro es opcional
    private PatientGender genero;
    private String nombreCompleto;
    private String dpi;
    private LocalDate fechaNacimiento;
    private String emailContacto;    // correo de contacto, no de autenticación
    private String direccion;
    private String telefono;
    private String contactoEmergencia;
    private String telefonoEmergencia;
    private String polizaSeguro;     // número de póliza individual del paciente

    public void validateDpiIfPresent() {
        if (dpi == null || dpi.isBlank()) {
            return;
        }
        if (!dpi.matches("^[0-9]{13}$")) {
            throw new IllegalArgumentException("El DPI del paciente debe tener exactamente 13 dígitos");
        }
    }
}

