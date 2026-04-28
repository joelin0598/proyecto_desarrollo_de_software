package his.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el registro de un nuevo paciente en el sistema.
 * Crea un registro en usuario_sistema (con rol PACIENTE) y en la tabla paciente.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPacienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El DPI es obligatorio")
    @Pattern(regexp = "^[0-9]{13}$", message = "El DPI debe contener exactamente 13 dígitos")
    private String dpi;
}
