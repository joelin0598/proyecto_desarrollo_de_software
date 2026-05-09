package his.infrastructure.persistence.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import his.domain.models.Patient;
import his.domain.models.Priority;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "signos_vitales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignsJpaEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long signosVitalesId;

    @Column(name = "cita_medica_id")
    private Long citaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PatientJpaEntity pacienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private HospitalStaffJpaEntity personalId;

    @Column(name = "presion_sistolica")
    private int presionSistolica;

    @Column(name = "presion_diastolica")
    private int presionDiastolica;

    @Column(name = "frecuencia_cardiaca")
    private int frecuenciaCardiaca;

    @Column(name = "temperatura")
    private double temperatura;

    @Column(name = "saturacion_oxigeno")
    private int saturacionOxigeno;

    @Column(name = "talla_cm")
    private double tallaCm;

    @Column(name = "peso_kg")
    private double pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad")
    private Priority priority;
}
