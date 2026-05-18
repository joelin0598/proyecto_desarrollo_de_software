package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "especialidad_medica")
@EqualsAndHashCode(callSuper = true)
public class MedicalSpecialtyCatalogJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "especialidad_id")
    private Long especialidadId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;
}

