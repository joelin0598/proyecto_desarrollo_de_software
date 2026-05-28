package his.application.dto;

import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CU 2.0 — Solicitud unificada de ingreso y triaje.
 * Combina ficha de paciente (FA01) + signos vitales (RN04).
 * El personalId se resuelve en el controlador desde el JWT del usuario autenticado.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageRequest {

    // Referencia opcional a una cita ya programada (flujo web -> llegada al hospital -> triaje)
    private Long citaMedicaId;

    // ── Datos personales del paciente ──────────────────────────────────────────
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre completo debe tener entre 5 y 150 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El DPI es obligatorio")
    @Pattern(regexp = "^[0-9]{13}$", message = "El DPI debe tener exactamente 13 digitos numericos")
    private String dpi;

    private LocalDate fechaNacimiento;

    @NotNull(message = "El genero es obligatorio")
    private PatientGender genero;

    @Email(message = "El correo electronico no tiene formato valido")
    private String emailContacto;

    @Pattern(regexp = "^[0-9]{8,15}$", message = "El telefono debe tener entre 8 y 15 digitos")
    private String telefono;

    @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
    private String direccion;

    // ── Contacto de emergencia ─────────────────────────────────────────────────
    @NotBlank(message = "El nombre del contacto de emergencia es obligatorio")
    @Size(max = 150)
    private String contactoEmergencia;

    @NotBlank(message = "El telefono del contacto de emergencia es obligatorio")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "El telefono de emergencia debe tener entre 8 y 15 digitos")
    private String telefonoEmergencia;

    // ── Seguro (opcional) ──────────────────────────────────────────────────────
    private Long aseguradoraId;    // null = sin seguro

    @Size(max = 80, message = "El numero de poliza no puede superar 80 caracteres")
    private String polizaSeguro;

    // ── Pago opcional para triaje walk-in ───────────────────────────────────────
    private PaymentOption metodoPago;

    @Size(max = 80, message = "El banco no puede exceder 80 caracteres")
    private String bancoTarjeta;

    @Pattern(regexp = "^[0-9]{13,19}$", message = "El numero de tarjeta debe contener entre 13 y 19 digitos")
    private String numeroTarjeta;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de vencimiento debe tener formato MM/yy")
    private String fechaVencimientoTarjeta;

    @Size(max = 120, message = "El nombre del titular no puede exceder 120 caracteres")
    private String nombreTitularTarjeta;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVC debe tener 3 o 4 digitos")
    private String cvc;

    // ── Signos vitales (RN04 / FA02) ───────────────────────────────────────────
    @Min(value = 50, message = "La presion sistolica esta fuera de rango clinico (50-300)")
    @Max(value = 300, message = "La presion sistolica esta fuera de rango clinico (50-300)")
    private int presionSistolica;

    @Min(value = 30, message = "La presion diastolica esta fuera de rango clinico (30-200)")
    @Max(value = 200, message = "La presion diastolica esta fuera de rango clinico (30-200)")
    private int presionDiastolica;

    @Min(value = 20, message = "La frecuencia cardiaca esta fuera de rango clinico (20-250)")
    @Max(value = 250, message = "La frecuencia cardiaca esta fuera de rango clinico (20-250)")
    private int frecuenciaCardiaca;

    @DecimalMin(value = "30.0", message = "La temperatura esta fuera de rango clinico (30-45)")
    @DecimalMax(value = "45.0", message = "La temperatura esta fuera de rango clinico (30-45)")
    private double temperatura;

    @Min(value = 50, message = "La saturacion de oxigeno esta fuera de rango clinico (50-100)")
    @Max(value = 100, message = "La saturacion de oxigeno esta fuera de rango clinico (50-100)")
    private int saturacionOxigeno;

    @DecimalMin(value = "1.0", message = "El peso esta fuera de rango clinico (1-500 kg)")
    @DecimalMax(value = "500.0", message = "El peso esta fuera de rango clinico (1-500 kg)")
    private double pesoKg;

    @DecimalMin(value = "30.0", message = "La talla esta fuera de rango clinico (30-300 cm)")
    @DecimalMax(value = "300.0", message = "La talla esta fuera de rango clinico (30-300 cm)")
    private double tallaCm;
}
