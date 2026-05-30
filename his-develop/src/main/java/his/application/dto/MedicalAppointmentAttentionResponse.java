package his.application.dto;

import his.domain.models.StatusAppointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CU06 — Respuesta de atención sobre una cita médica.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointmentAttentionResponse {
    private Long citaMedicaDetalleId;
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private Long personalId;
    private String medicoNombre;
    private StatusAppointment estado;
    private String evaluacionFisica;
    private String diagnostico;
    private String ordenLaboratorio;
    private String recetaMedica;
    private String medicacionPrescrita;
    private Boolean requiereSeguimiento;
    private Long citaSeguimientoId;
    private LocalDateTime createdAt;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String especialidadNombre;
    private String prioridad;
}

