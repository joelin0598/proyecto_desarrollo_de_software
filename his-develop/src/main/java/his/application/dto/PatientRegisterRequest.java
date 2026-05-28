package his.application.dto;

import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegisterRequest {
    @NotBlank
    @Size(min = 5, max = 150)
    private String nombreCompleto;

    @NotBlank
    @Pattern(regexp = "^[0-9]{13}$")
    private String dpi;

    private LocalDate fechaNacimiento;

    @NotNull
    private PatientGender genero;

    @Pattern(regexp = "^[0-9]{8,15}$")
    private String telefono;

    @Email
    private String emailContacto;

    @Size(max = 255)
    private String direccion;

    @NotBlank
    @Size(max = 150)
    private String contactoEmergencia;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8,15}$")
    private String telefonoEmergencia;

    private PaymentOption metodoPago;
    private String bancoTarjeta;
    private String numeroTarjeta;
    private String fechaVencimientoTarjeta;
    private String nombreTitularTarjeta;
    private String cvc;
    private Long aseguradoraId;
    private String polizaSeguro;
}

