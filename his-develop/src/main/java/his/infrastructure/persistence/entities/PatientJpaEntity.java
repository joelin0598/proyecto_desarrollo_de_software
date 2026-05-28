package his.infrastructure.persistence.entities;

import his.domain.models.PatientGender;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paciente")
@EqualsAndHashCode(callSuper = true)
public class PatientJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paciente_id")
    private Long pacienteId;

    // nullable=true: pacientes de triaje presencial no tienen cuenta web todavía
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private UserJpaEntity usuarioSistema;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "dpi", unique = true, length = 13)
    private String dpi;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero_id", nullable = false)
    private PatientGender genero;

    // nullable=true: seguro es opcional (el paciente puede no tener aseguradora)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aseguradora_id", nullable = true)
    private InsuranceCatalogJpaEntity aseguradora;

    @Column(name = "poliza_seguro", length = 80)
    private String polizaSeguro;

    @Column(name = "fecha_nacimiento_paciente")
    private LocalDate fechaNacimiento;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    @Column(name = "direccion_paciente", length = 255)
    private String direccion;

    @Column(name = "telefono_paciente", length = 20)
    private String telefono;

    @Column(name = "nombre_contacto_emergencia", length = 150)
    private String contactoEmergencia;

    @Column(name = "telefono_emergencia", length = 20)
    private String telefonoEmergencia;
}

