package his.infrastructure.persistence.entities;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PaymentOption;
import his.domain.models.Priority;
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
import java.time.LocalDateTime;
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
    @JoinColumn(name = "personal_id", nullable = true)
    private HospitalStaffJpaEntity personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id")
    private MedicalSpecialityCatalogJpaEntity especialidad;

    @Column(name = "fecha_cita", nullable = true)
    private LocalDate fechaCita;

    @Column(name = "hora_cita", nullable = true)
    private LocalTime horaCita;

    @Column(name = "motivo_consulta", nullable = false, length = 500)
    private String motivoConsulta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = true)
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

    @Column(name = "solvencia_pago")
    private Boolean solvenciaPago;

    @Column(name = "cita_programada")
    private Boolean citaProgramada;

    @Column(name = "codigo_cita", length = 60, unique = true)
    private String codigoCita;

    @Column(name = "qr_contenido", length = 500)
    private String qrContenido;

    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "temperatura")
    private Double temperatura;

    @Column(name = "saturacion_oxigeno")
    private Integer saturacionOxigeno;

    @Column(name = "talla_cm")
    private Double tallaCm;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 20)
    private Priority prioridad;

    @Column(name = "alerta_emergencia")
    private Boolean alertaEmergencia;

    @Column(name = "fecha_hora_triaje")
    private LocalDateTime fechaHoraTriaje;
}
