package his.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cita_medica_detalle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MedicalAppointmentDetailsJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_medica_detalle_id")
    private Long citaMedicaDetalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_medica_id", nullable = false)
    private MedicalAppointmentJpaEntity citaMedica;

    @Column(name = "evaluacion_fisica", length = 1000)
    private String evaluacionFisica;

    @Column(name = "diagnostico", length = 1000)
    private String diagnostico;

    @Column(name = "orden_laboratorio", length = 1000)
    private String ordenLaboratorio;

    @Column(name = "receta_medica", length = 1000)
    private String recetaMedica;

    @Column(name = "medicacion_prescrita", length = 1000)
    private String medicacionPrescrita;

    @Column(name = "requiere_seguimiento")
    private Boolean requiereSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_seguimiento_id")
    private MedicalAppointmentJpaEntity citaSeguimiento;
}

