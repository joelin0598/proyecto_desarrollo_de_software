package his.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa una transacción de cobro (CU-05).
 * Registra el método de pago, montos, cobertura de seguro y estado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointment_id")
    private AppointmentEntity appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "authorization_number")
    private String authorizationNumber;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "insurance_coverage")
    private Double insuranceCoverage;

    @Column(name = "pending_balance")
    private Double pendingBalance;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "emergency_bypass")
    private Boolean emergencyBypass;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "audit_note")
    private String auditNote;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
