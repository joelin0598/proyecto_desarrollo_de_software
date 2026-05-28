package his.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "receta_medica")
@EqualsAndHashCode(callSuper = true)
public class MedicalPrescriptionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receta_medica_id")
    private Long recetaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_medica_detalle_id", nullable = false)
    private MedicalAppointmentDetailsJpaEntity citaMedicaDetalle;

    @Column(name = "instrucciones_generales", length = 1000)
    private String instruccionesGenerales;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
}

