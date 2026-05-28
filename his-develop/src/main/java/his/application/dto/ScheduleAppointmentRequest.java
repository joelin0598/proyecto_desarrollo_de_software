package his.application.dto;

import his.domain.models.PaymentOption;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleAppointmentRequest {

    private Long pacienteId;

    @Pattern(regexp = "^[0-9]{13}$", message = "El DPI debe tener exactamente 13 digitos")
    private String dpiPaciente;

    @NotNull(message = "El medico es obligatorio")
    private Long medicoPersonalId;

    private Long especialidadId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser futura")
    private LocalDate fechaCita;

    @NotNull(message = "La hora de la cita es obligatoria")
    private LocalTime horaCita;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    private String motivoConsulta;

    @NotNull(message = "Debe seleccionar metodo de pago")
    private PaymentOption metodoPago;

    // Tarjeta (RN08)
    @Size(max = 80, message = "El banco no puede exceder 80 caracteres")
    private String bancoTarjeta;

    @Pattern(regexp = "^[0-9]{13,19}$", message = "El numero de tarjeta debe contener entre 13 y 19 digitos")
    private String numeroTarjeta;

    // Formato MM/yy (simulacion)
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de vencimiento debe tener formato MM/yy")
    private String fechaVencimientoTarjeta;

    @Size(max = 120, message = "El nombre del titular no puede exceder 120 caracteres")
    private String nombreTitularTarjeta;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVC debe tener 3 o 4 digitos")
    private String cvc;

    // Seguro (RN08)
    private Long aseguradoraId;

    @Size(max = 80, message = "El numero de poliza no puede exceder 80 caracteres")
    private String numeroPoliza;
}
