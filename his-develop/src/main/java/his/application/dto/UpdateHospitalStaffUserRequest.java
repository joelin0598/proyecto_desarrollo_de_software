package his.application.dto;

import his.domain.models.Role;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHospitalStaffUserRequest {
    @Size(min = 5, max = 150, message = "El nombre completo debe tener entre 5 y 150 caracteres")
    private String nombreCompleto;

    @Size(min = 5, max = 255, message = "La direccion debe tener entre 5 y 255 caracteres")
    private String direccion;

    @Pattern(regexp = "^[0-9]{8,15}$", message = "El telefono corporativo debe contener solo numeros (8-15 digitos)")
    private String telefonoCorporativo;

    private Long especialidadId;

    private Long unidadAtencionId;

    private Role rol;

    @Size(max = 20, message = "El numero de colegiado no puede exceder 20 caracteres")
    private String numeroColegiado;
}

