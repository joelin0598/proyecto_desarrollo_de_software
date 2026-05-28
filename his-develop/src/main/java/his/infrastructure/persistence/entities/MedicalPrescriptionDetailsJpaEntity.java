package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "receta_medica_detalle")
@EqualsAndHashCode(callSuper = true)
public class MedicalPrescriptionDetailsJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receta_medica_detalle_id")
    private Long recetaMedicaDetalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_medica_id", nullable = false)
    private MedicalPrescriptionJpaEntity recetaMedica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private MedicineJpaEntity medicamento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "dosis", length = 100)
    private String dosis;

    @Column(name = "via_administracion", length = 80)
    private String viaAdministracion;

    @Column(name = "frecuencia_horas")
    private Integer frecuenciaHoras;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "despachado")
    private Boolean despachado;

    @Column(name = "pago_validado")
    private Boolean pagoValidado;
}

