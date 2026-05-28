package his.infrastructure.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Size(min = 13, max = 13)
    @Column(name = "dpi", nullable = false, unique = true)
    private String dpi;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero")
    private String genero;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
