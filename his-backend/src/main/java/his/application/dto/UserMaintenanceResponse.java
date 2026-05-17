package his.application.dto;

import his.domain.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMaintenanceResponse {
    private Long userId;
    private String email;
    private Role role;
    private boolean active;

    private Long personalId;
    private String nombreCompleto;
    private String numeroColegiado;
    private String telefonoCorporativo;
    private String direccion;
    private Long especialidadId;
    private Long unidadAtencionId;
}
