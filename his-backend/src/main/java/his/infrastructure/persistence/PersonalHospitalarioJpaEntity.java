package his.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import his.domain.Role;
import his.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que mapea la tabla 'personal_hospitalario' en la base de datos.
 * Es el espejo SQL del modelo de dominio {@link his.domain.model.PersonalHospitalario}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "usuario")
@Builder
@Entity
@Table(name = "personal_hospitalario")
public class PersonalHospitalarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_id")
    private Long personalId;

    @Column(name = "dpi", unique = true)
    private String dpi;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "numero_colegiado")
    private String numeroColegiado;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Role rol;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity usuario;
}
