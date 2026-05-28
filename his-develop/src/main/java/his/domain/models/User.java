package his.domain.models;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long userId;
    private String email;
    private String password;
    private Role role;
    private boolean active;

    public void validateForAuthentication() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }
}
