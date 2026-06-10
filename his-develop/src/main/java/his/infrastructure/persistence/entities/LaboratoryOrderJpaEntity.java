package his.infrastructure.persistence.entities;

import his.domain.models.LaboratoryOrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_laboratorio")
@EqualsAndHashCode(callSuper = true)
public class LaboratoryOrderJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orden_laboratorio_id")
    private Long ordenLaboratorioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_medica_detalle_id", nullable = false)
    private MedicalAppointmentDetailsJpaEntity citaMedicaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id")
    private HospitalStaffJpaEntity personal;

    @Column(name = "nombre_examen", nullable = false, length = 200)
    private String nombreExamen;

    @Column(name = "tipo_muestra", length = 100)
    private String tipoMuestra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private LaboratoryOrderStatus estado;

    @Builder.Default
    @Column(name = "pago_validado", nullable = false)
    private Boolean pagoValidado = false;

    @Column(name = "etiqueta_id", length = 80)
    private String etiquetaId;

    @Builder.Default
    @Column(name = "alerta_critica", nullable = false)
    private Boolean alertaCritica = false;

    @Column(name = "observaciones_tecnico", length = 500)
    private String observacionesTecnico;
}

