package his.domain.ports;

import his.domain.UserEntity;

import java.util.Optional;

/**
 * Puerto de salida (repositorio) para el dominio de usuario del sistema.
 * Define el contrato de persistencia que la capa de aplicación utiliza
 * sin acoplarse a ningún framework de persistencia (JPA, JDBC, etc.).
 */
public interface UserRepository {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<UserEntity> findUserByEmail(String email);

    /**
     * Persiste un usuario en el sistema.
     *
     * @param user Entidad de usuario a guardar
     * @return El usuario guardado con su ID asignado
     */
    UserEntity save(UserEntity user);
}
