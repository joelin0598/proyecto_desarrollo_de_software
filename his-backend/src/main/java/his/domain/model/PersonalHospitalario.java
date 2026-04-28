package his.domain.model;

import his.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio puro para el Personal Hospitalario.
 * Representa la abstracción de negocio de la entidad 'personal_hospitalario'
 * sin dependencias de frameworks de persistencia o seguridad.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalHospitalario {

    private Long personalId;

    /** ID del usuario del sistema al que pertenece este perfil de personal. */
    private Long usuarioId;

    /** Documento Personal de Identificación (13 dígitos). Debe ser único (RN11). */
    private String dpi;

    /** Dirección de residencia del personal. */
    private String direccion;

    /** Número de colegiado profesional (para médicos). */
    private String numeroColegiado;

    /** Rol/cargo del personal en el hospital. */
    private Role rol;

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

    /**
     * Valida que el número de colegiado tenga un formato válido (cuando aplica).
     *
     * @throws IllegalArgumentException si el número de colegiado no cumple el formato
     */
    public void validarNumeroColegiado() {
        if (numeroColegiado != null && !numeroColegiado.isBlank() && !numeroColegiado.matches("^[0-9]{4,10}$")) {
            throw new IllegalArgumentException("El número de colegiado debe contener entre 4 y 10 dígitos.");
        }
    }
}
