package his.infrastructure.persistence.entities;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PaymentOption;
import his.domain.models.StatusAppointment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cita_medica")
@EqualsAndHashCode(callSuper = true)
public class MedicalAppointmentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cita_medica_id")
    private Long citaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PatientJpaEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private HospitalStaffJpaEntity personal;

    @Column(name = "especialidad_id")
    private Long especialidadId;

    @Column(name = "fecha_cita", nullable = false)
    private LocalDate fechaCita;

    @Column(name = "hora_cita", nullable = false)
    private LocalTime horaCita;

    @Column(name = "motivo_consulta", nullable = false, length = 500)
    private String motivoConsulta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private PaymentOption metodoPago;

    @Column(name = "costo_consulta", nullable = false)
    private Double costoConsulta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cita", nullable = false)
    private StatusAppointment estadoCita;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_administrativo", nullable = false)
    private AdministrativeAppointmentStatus estadoAdministrativo;

    @Column(name = "observacion_administrativa", length = 300)
    private String observacionAdministrativa;
}
