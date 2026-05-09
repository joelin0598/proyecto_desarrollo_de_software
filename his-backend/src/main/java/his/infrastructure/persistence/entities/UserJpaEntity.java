package his.infrastructure.persistence.entities;

import his.domain.models.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_sistema")
@EqualsAndHashCode(callSuper = true)
public class UserJpaEntity extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Role rol;

    @Column(name = "email_paciente", nullable = false, unique = true)
    private String emailPaciente;

    @Column(name = "password_usuario", nullable = false)
    private String passwordUsuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordUsuario;
    }

    @Override
    public String getUsername() {
        return emailPaciente;
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(getIsActive());
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(getIsActive());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(getIsActive());
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(getIsActive());
    }
}

