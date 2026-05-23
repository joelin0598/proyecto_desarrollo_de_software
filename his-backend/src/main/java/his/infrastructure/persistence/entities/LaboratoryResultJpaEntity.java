package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resultado_laboratorio")
@EqualsAndHashCode(callSuper = true)
public class LaboratoryResultJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_laboratorio_id")
    private Long resultadoLaboratorioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_laboratorio_id", nullable = false)
    private LaboratoryOrderJpaEntity ordenLaboratorio;

    @Column(name = "nombre_examen", length = 200)
    private String nombreExamen;

    @Column(name = "valor_resultado", precision = 12, scale = 4)
    private BigDecimal valorResultado;

    @Column(name = "unidad_resultado", length = 50)
    private String unidadResultado;

    @Column(name = "referencia_minima", precision = 12, scale = 4)
    private BigDecimal referenciaMinima;

    @Column(name = "referencia_maxima", precision = 12, scale = 4)
    private BigDecimal referenciaMaxima;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "resumen", length = 1000)
    private String resumen;

    @Column(name = "conclusion", length = 1000)
    private String conclusion;

    @Column(name = "critico")
    private Boolean critico;
}

