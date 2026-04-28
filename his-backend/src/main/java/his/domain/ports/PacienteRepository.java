package his.domain.ports;

import his.domain.model.Paciente;

import java.util.Optional;

/**
 * Puerto de salida (repositorio) para el dominio de Paciente.
 * Define el contrato de persistencia sin acoplarse a ningún framework.
 */
public interface PacienteRepository {

    /**
     * Persiste un paciente en el sistema.
     *
     * @param paciente Modelo de dominio del paciente a guardar
     * @return El paciente guardado con su ID asignado
     */
    Paciente save(Paciente paciente);

    /**
     * Busca un paciente por su DPI. Utilizado para aplicar RN11 (unicidad de identidad).
     *
     * @param dpi Documento Personal de Identificación (13 dígitos)
     * @return Optional con el paciente si existe, vacío si no
     */
    Optional<Paciente> findByDpi(String dpi);

    /**
     * Busca el paciente asociado a un usuario del sistema.
     *
     * @param usuarioId ID del usuario del sistema
     * @return Optional con el paciente si existe, vacío si no
     */
    Optional<Paciente> findByUsuarioId(Long usuarioId);
}
