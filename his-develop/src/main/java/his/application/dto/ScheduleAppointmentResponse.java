package his.application.dto;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PaymentOption;
import his.domain.models.StatusAppointment;
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
public class ScheduleAppointmentResponse {
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteIdentificacion;
    private Long medicoPersonalId;
    private String medicoNombre;
    private Long especialidadId;
    private String especialidadNombre;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String motivoConsulta;
    private PaymentOption metodoPago;
    private Double costoConsulta;
    private StatusAppointment estadoCita;
    private AdministrativeAppointmentStatus estadoAdministrativo;
    private boolean pagoValidado;
    private String transaccionId;
    private String codigoCita;
    private String qrContenido;
    private String mensajeValidacion;
}


