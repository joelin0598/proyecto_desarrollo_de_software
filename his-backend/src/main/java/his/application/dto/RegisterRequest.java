package his.application.dto;

import his.domain.models.PatientGender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre completo debe tener entre 5 y 150 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El DPI es obligatorio")
    @Pattern(regexp = "^[0-9]{13}$", message = "El DPI debe contener exactamente 13 dígitos")
    private String dpi;

    @NotNull(message = "El genero es obligatorio")
    private PatientGender genero;

    private LocalDate fechaNacimiento;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    @Pattern(regexp = "^[0-9]{8,15}$", message = "El teléfono debe contener solo números (8-15 dígitos)")
    private String telefono;

    @Size(max = 150, message = "El contacto de emergencia no puede exceder 150 caracteres")
    private String contactoEmergencia;

    @Pattern(regexp = "^$|^[0-9]{8,15}$", message = "El teléfono de emergencia debe contener solo números (8-15 dígitos)")
    private String telefonoEmergencia;

    private Long aseguradoraId;
}
