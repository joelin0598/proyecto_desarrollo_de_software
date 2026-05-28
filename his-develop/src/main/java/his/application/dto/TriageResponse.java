package his.application.dto;

import his.domain.models.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CU 2.0 — Respuesta del ingreso y triaje.
 * Retorna datos del paciente registrado/encontrado + signos vitales + prioridad calculada en dominio.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TriageResponse {

    // Resultado del paciente
    private Long pacienteId;
    private String nombreCompleto;
    private String dpi;
    private boolean pacienteNuevo;      // true = FA01 (primera visita), false = paciente existente

    // Resultado del triaje
    private Long signosVitalesId;
    private Long citaMedicaId;
    private Priority prioridad;         // calculada en dominio (RN04), NO en frontend
    private boolean alertaEmergencia;   // true = FA03 (prioridad ROJO)
    private boolean pagoValidado;
    private String mensajePago;

    // Signos vitales registrados
    private int presionSistolica;
    private int presionDiastolica;
    private int frecuenciaCardiaca;
    private double temperatura;
    private int saturacionOxigeno;
    private double pesoKg;
    private double tallaCm;
}


