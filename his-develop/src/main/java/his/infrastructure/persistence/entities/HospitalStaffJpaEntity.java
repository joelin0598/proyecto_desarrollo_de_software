package his.infrastructure.persistence.entities;

import his.domain.models.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "personal_hospitalario")
@EqualsAndHashCode(callSuper = true)
public class HospitalStaffJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_id")
    private Long personalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserJpaEntity usuarioSistema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id")
    private MedicalSpecialityCatalogJpaEntity especialidad;

    @Column(name = "unidad_atencion_id")
    private Long unidadAtencionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Role rol;

    @Column(name = "numero_colejiado", unique = true, length = 20)
    private String numeroColejiado;

    @Column(name = "telefono_corporativo", length = 20)
    private String telefonoCorporativo;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "direccion", length = 255)
    private String direccion;
}

