package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CU06 / RN09 — Item de la cola de espera de atención sobre citas médicas.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointmentQueueItemResponse {
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String especialidadNombre;
    private String prioridad;
    private boolean alertaEmergencia;
    private String tipoAtencion;
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private Integer frecuenciaCardiaca;
    private Double temperatura;
    private Integer saturacionOxigeno;
    private String estadoAdministrativo;
}

