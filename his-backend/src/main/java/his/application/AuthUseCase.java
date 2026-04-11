package his.domain.ports;

import his.application.dto.AuthResponse;
import his.application.dto.AuthenticationRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;

/**
 * Puerto de aplicación que define el contrato para los casos de uso de autenticación y registro.
 *
 * Esta interfaz representa la "entrada" de la capa de aplicación desde la arquitectura hexagonal.
 * Los adaptadores (REST controllers) la utilizan para ejecutar operaciones de autenticación.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Registrar nuevos usuarios con rol USER (pacientes/clientes)</li>
 *   <li>Registrar nuevos administradores con rol ADMIN (médicos/personal)</li>
 *   <li>Autenticar usuarios existentes generando tokens JWT</li>
 * </ul>
 *
 * <p><b>Precondiciones:</b>
 * <ul>
 *   <li>El email debe ser único en el sistema</li>
 *   <li>La contraseña debe cumplir política: mín 6 caracteres, 1 mayúscula, 1 número, 1 símbolo especial</li>
 *   <li>Los datos de entrada deben ser validados antes de llamar estos métodos (@Valid en DTOs)</li>
 * </ul>
 *
 * <p><b>Postcondiciones (en caso de éxito):</b>
 * <ul>
 *   <li>El usuario se crea en la base de datos (rol, email, contraseña encriptada)</li>
 *   <li>Se genera un token JWT válido por 24 horas</li>
 *   <li>Se retorna información del usuario y su token</li>
 * </ul>
 *
 * <p><b>Excepciones que lanza:</b>
 * <ul>
 *   <li>{@code DuplicateEmailException} - Si el email ya existe</li>
 *   <li>{@code InvalidPasswordFormatException} - Si la contraseña no cumple requisitos</li>
 *   <li>{@code CustomAuthenticationException} - Si falla la autenticación</li>
 * </ul>
 *
 * <p><b>Transaccionalidad:</b>
 * Los métodos registro son transaccionales (rollback si falla crear usuario o relaciones).
 * El método autenticar es transaccional read-only (solo lectura).
 *
 * @see his.application.AuthService Implementación de este contrato
 * @see his.adapters.rest.AuthController Adaptador REST que utiliza este puerto
 */
public interface AuthUseCase {

    /**
     * Registra un nuevo usuario con rol USER.
     *
     * <p>Crea:
     * <ul>
     *   <li>Una entidad UserEntity con rol USER</li>
     *   <li>Una entidad UserGenericEntityVisit vinculada (relación 1:1)</li>
     *   <li>Un token JWT con claims: role=USER, sub=email, idUser=id del cliente</li>
     * </ul>
     *
     * @param request DTO con firstName, lastName, email, password
     * @return AuthResponse con token JWT y datos del usuario
     * @throws DuplicateEmailException si el email ya existe
     * @throws InvalidPasswordFormatException si la contraseña no cumple formato
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Registra un nuevo administrador con rol ADMIN.
     *
     * <p>Crea:
     * <ul>
     *   <li>Una entidad UserEntity con rol ADMIN</li>
     *   <li>Una entidad UserGenericEntity vinculada con datos adicionales (DPI, dirección, teléfono)</li>
     *   <li>Un token JWT con claims: role=ADMIN, sub=email, idUser=id del staff</li>
     * </ul>
     *
     * @param requestAdmin DTO con firstName, lastName, email, password, dpi, direccion, telefono
     * @return AuthResponse con token JWT y datos del administrador
     * @throws DuplicateEmailException si el email ya existe
     * @throws InvalidPasswordFormatException si la contraseña no cumple formato
     */
    AuthResponse registerAdmin(RegisterRequestAdmin requestAdmin);

    /**
     * Autentica un usuario existente.
     *
     * <p>Valida:
     * <ul>
     *   <li>Que el usuario existe con ese email</li>
     *   <li>Que la contraseña coincide (validación BCrypt)</li>
     *   <li>Genera nuevo token JWT con claims según el rol del usuario</li>
     * </ul>
     *
     * @param request DTO con email y password
     * @return AuthResponse con token JWT y datos del usuario autenticado
     * @throws CustomAuthenticationException si el usuario no existe o contraseña es incorrecta
     */
    AuthResponse authenticate(AuthenticationRequest request);
}
