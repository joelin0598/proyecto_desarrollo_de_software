package his.domain;

import his.infrastructure.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "patients")
public class PatientEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8,15}$")
    @Column(name = "phone")
    private String phone;

    @NotNull
    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "gender")
    private String gender;

    @NotBlank
    @Size(min = 5, max = 255)
    @Column(name = "address")
    private String address;

    @NotBlank
    @Pattern(regexp = "^[0-9]{13}$")
    @Column(name = "dpi", unique = true)
    private String dpi;

    @Column(name = "emergency_phone")
    private String emergencyPhone;
}
