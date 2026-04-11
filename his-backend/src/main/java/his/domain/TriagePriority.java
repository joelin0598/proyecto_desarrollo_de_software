package his.domain;

/**
 * Nivel de prioridad asignado durante el triaje (CU-2 / RN04).
 * RED    – Código Rojo: riesgo inminente para la vida.
 * ORANGE – Urgente: requiere atención rápida.
 * GREEN  – Estándar: no urgente.
 */
public enum TriagePriority {
    RED, ORANGE, GREEN
}
