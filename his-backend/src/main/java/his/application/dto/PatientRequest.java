package his.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 255, message = "El apellido debe tener entre 2 y 255 caracteres")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "El teléfono debe contener solo números (8-15 dígitos)")
    private String phone;

    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    private String dateOfBirth;

    @NotBlank(message = "El género es obligatorio")
    private String gender;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;

    @NotBlank(message = "El DPI es obligatorio")
    @Pattern(regexp = "^[0-9]{13}$", message = "El DPI debe contener exactamente 13 dígitos")
    private String dpi;

    private String emergencyPhone;
}
