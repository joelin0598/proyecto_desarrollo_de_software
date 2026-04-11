package his.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa una cita médica (CU-04).
 * Incluye datos del paciente, especialidad, médico, horario,
 * motivo de consulta y datos de cobertura de seguro.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private UserEntity patient;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private String appointmentTime;

    @Column(name = "reason", nullable = false)
    private String reason;

    // Datos de cobertura de seguro
    @Column(name = "insurer_name")
    private String insurerName;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "holder_dpi")
    private String holderDpi;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "base_tariff")
    private Double baseTariff;

    @Column(name = "deductible")
    private Double deductible;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "audit_note")
    private String auditNote;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
