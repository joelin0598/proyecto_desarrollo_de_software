package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicamento")
@EqualsAndHashCode(callSuper = true)
public class MedicineJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicamento_id")
    private Long medicamentoId;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "precio_unitario")
    private Double precioUnitario;
}

