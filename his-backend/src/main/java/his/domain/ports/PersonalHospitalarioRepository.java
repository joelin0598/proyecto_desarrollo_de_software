package his.domain.ports;

import his.domain.model.PersonalHospitalario;

import java.util.Optional;

/**
 * Puerto de salida (repositorio) para el dominio de Personal Hospitalario.
 * Define el contrato de persistencia sin acoplarse a ningún framework.
 */
public interface PersonalHospitalarioRepository {

    /**
     * Persiste un registro de personal hospitalario en el sistema.
     *
     * @param personal Modelo de dominio del personal a guardar
     * @return El personal guardado con su ID asignado
     */
    PersonalHospitalario save(PersonalHospitalario personal);

    /**
     * Busca personal por su DPI. Utilizado para aplicar RN11 (unicidad de identidad).
     *
     * @param dpi Documento Personal de Identificación (13 dígitos)
     * @return Optional con el personal si existe, vacío si no
     */
    Optional<PersonalHospitalario> findByDpi(String dpi);

    /**
     * Busca el personal asociado a un usuario del sistema.
     *
     * @param usuarioId ID del usuario del sistema
     * @return Optional con el personal si existe, vacío si no
     */
    Optional<PersonalHospitalario> findByUsuarioId(Long usuarioId);
}
