package his.application.dto;

import his.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 👤 Contiene la información básica del usuario autenticado.
 * Esta clase se envía al frontend junto al token JWT.
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
