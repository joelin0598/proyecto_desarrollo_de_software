package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VitalSigns {

    private Long signosVitalesId;
    private Long pacienteId;//nuevo
    private Long personalId;//nuevo
    private Long citaMedicaId;//nullable
    private int presionSistolica;
    private int presionDiastolica;
    private int frecuenciaCardiaca;
    private double temperatura;
    private int saturacionOxigeno;
    private double tallaCm;
    private double pesoKg;
    private Priority priority;
    private LocalDateTime createdAt;

    /**
     * RN04 – Clasificación de Prioridad (Triaje)
     * Lógica interna del dominio para determinar el nivel de urgencia.
     */
    public void calculatePriority() {
        // Lógica de ejemplo basada en estándares médicos:
        if (this.saturacionOxigeno < 85 || (this.temperatura >= 40)) {
            this.priority = Priority.ROJO; // Emergencia inmediata
        } else if (this.saturacionOxigeno < 92 || this.temperatura >= 38.5) {
            this.priority = Priority.NARANJA; // Urgencia
        }  else if (this.saturacionOxigeno < 95 || this.temperatura >= 37.5) {
            this.priority = Priority.AMARILLO; // Urgencia moderada
        } else {
            this.priority = Priority.VERDE; // Urgencia menor / Estable
        }
    }

}


