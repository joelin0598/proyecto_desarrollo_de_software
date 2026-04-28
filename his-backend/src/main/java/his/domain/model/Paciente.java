package his.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio puro para un Paciente.
 * Representa la abstracción de negocio de la entidad 'paciente'
 * sin dependencias de frameworks de persistencia o seguridad.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Paciente {

    private Long pacienteId;

    /** ID del usuario del sistema al que pertenece este perfil de paciente. */
    private Long usuarioId;

    /** Documento Personal de Identificación (13 dígitos). Debe ser único (RN11). */
    private String dpi;

    /**
     * Valida que el DPI tenga el formato correcto (exactamente 13 dígitos numéricos).
     *
     * @throws IllegalArgumentException si el DPI no cumple el formato requerido
     */
    public void validarDpi() {
        if (dpi == null || !dpi.matches("^[0-9]{13}$")) {
            throw new IllegalArgumentException("El DPI debe contener exactamente 13 dígitos numéricos.");
        }
    }
}
