package his.application.dto;

import his.domain.models.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestAdmin {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre completo debe tener entre 5 y 150 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{1,50}$", message = "El nombre solo puede contener letras y espacios (máx 50)")
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String direccion;

    @NotBlank(message = "El teléfono corporativo es obligatorio")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "El teléfono corporativo debe contener solo números (8-15 dígitos)")
    private String telefonoCorporativo;

    private Long especialidadId;

    private Long unidadAtencionId;

    @NotNull(message = "El rol es obligatorio")
    private Role rol;

    @Size(max = 20, message = "El número de colegiado no puede exceder 20 caracteres")
    private String numeroColegiado;
}
