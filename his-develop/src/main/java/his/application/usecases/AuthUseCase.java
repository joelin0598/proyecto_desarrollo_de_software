package his.application.usecases;

import his.application.dto.AuthResponse;
import his.application.dto.AuthenticationRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.services.AuthService;

/**
 * Puerto de aplicación para autenticación y registro.
 *
 * Esta interfaz representa la entrada de la capa de aplicación en arquitectura hexagonal.
 * Los adaptadores REST la usan para ejecutar operaciones sobre usuario_sistema,
 * paciente y personal_hospitalario.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Registrar pacientes en usuario_sistema + paciente</li>
 *   <li>Registrar personal hospitalario en usuario_sistema + personal_hospitalario</li>
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
 *   <li>Se crea el registro de usuario_sistema con rol, email y contraseña cifrada</li>
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
 * @see AuthService Implementación de este contrato
 * @see his.adapters.rest.AuthController Adaptador REST que utiliza este puerto
 */
public interface AuthUseCase {

    /**
     * Registra un nuevo usuario de tipo paciente.
     *
     * <p>Crea:
     * <ul>
     *   <li>Una entidad UserEntity con rol PACIENTE</li>
     *   <li>Una entidad Patient vinculada por usuarioId</li>
     *   <li>Un token JWT con claims de autenticación</li>
     * </ul>
     *
     * @param request DTO con firstName, lastName, email, password
     * @return AuthResponse con token JWT y datos del usuario
     * @throws DuplicateEmailException si el email ya existe
     * @throws InvalidPasswordFormatException si la contraseña no cumple formato
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Registra personal hospitalario.
     *
     * <p>Crea:
     * <ul>
     *   <li>Una entidad UserEntity con rol de personal (fase actual: ADMIN)</li>
     *   <li>Una entidad HospitalStaff vinculada con datos laborales</li>
     *   <li>Un token JWT con claims de autenticación</li>
     * </ul>
     *
     * @param requestAdmin DTO con firstName, lastName, email, password, dpi, direccion, telefono
     * @return AuthResponse con token JWT y datos del personal hospitalario
     * @throws DuplicateEmailException si el email ya existe
     * @throws InvalidPasswordFormatException si la contraseña no cumple formato
     */
    AuthResponse registerPersonal(RegisterRequestAdmin requestAdmin);

    /**
     * Alias temporal para mantener compatibilidad con endpoints existentes.
     */
    @Deprecated
    default AuthResponse registerAdmin(RegisterRequestAdmin requestAdmin) {
        return registerPersonal(requestAdmin);
    }

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

