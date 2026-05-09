package his.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "aseguradora")
@EqualsAndHashCode(callSuper = true)
public class InsuranceCatalogJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aseguradora_id")
    private Long aseguradoraId;

    @Column(name = "nombre_aseguradora", nullable = false, length = 150)
    private String nombreAseguradora;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "poliza_seguro", length = 50)
    private String polizaSeguro;
}

