package his.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de triaje (CU-2): almacena signos vitales y la prioridad
 * asignada automáticamente según RN04.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "triage_records")
public class TriageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "triage_id")
    private Long triageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Signos vitales
    @Column(name = "systolic_pressure")
    private Double systolicPressure;

    @Column(name = "diastolic_pressure")
    private Double diastolicPressure;

    @Column(name = "heart_rate")
    private Double heartRate;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "oxygen_saturation")
    private Double oxygenSaturation;

    @Column(name = "weight")
    private Double weight;

    // Clasificación de urgencia (RN04)
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TriagePriority priority;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "registered_by")
    private String registeredBy;

    @PrePersist
    public void prePersist() {
        if (arrivalTime == null) {
            arrivalTime = LocalDateTime.now();
        }
    }
}
