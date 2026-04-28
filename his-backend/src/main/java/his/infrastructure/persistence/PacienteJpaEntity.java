package his.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import his.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que mapea la tabla 'paciente' en la base de datos.
 * Es el espejo SQL del modelo de dominio {@link his.domain.model.Paciente}.
 *
 * <p>Relación: Many pacientes pueden estar asociados a un usuario del sistema,
 * aunque en la práctica un usuario del sistema tiene un único perfil de paciente (1:1).</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "usuario")
@Builder
@Entity
@Table(name = "paciente")
public class PacienteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paciente_id")
    private Long pacienteId;

    @Column(name = "dpi", unique = true)
    private String dpi;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity usuario;
}
