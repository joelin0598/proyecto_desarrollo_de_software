package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recordatorio_medicamento")
@EqualsAndHashCode(callSuper = true)
public class MedicationReminderJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recordatorio_id")
    private Long recordatorioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_medica_detalle_id", nullable = false)
    private MedicalPrescriptionDetailsJpaEntity recetaMedicaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PatientJpaEntity paciente;

    @Column(name = "medicamento_nombre", length = 200)
    private String medicamentoNombre;

    @Column(name = "dosis", length = 100)
    private String dosis;

    @Column(name = "frecuencia_horas")
    private Integer frecuenciaHoras;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "via_administracion", length = 80)
    private String viaAdministracion;

    @Column(name = "proximo_recordatorio")
    private LocalDateTime proximoRecordatorio;

    @Column(name = "activo")
    private Boolean activo;
}

