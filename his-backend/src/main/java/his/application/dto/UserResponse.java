package his.application.dto;

import his.domain.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 👤 Datos básicos del usuario_sistema autenticado.
 * Se envía al frontend junto al token JWT.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
