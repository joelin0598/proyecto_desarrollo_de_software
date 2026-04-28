package his.domain.ports;

import his.application.dto.AuthResponse;
import his.application.dto.RegisterPersonalRequest;

/**
 * Puerto de entrada (caso de uso) para el registro de personal hospitalario.
 * Esta operación es realizada por un Administrador.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Registrar nuevo personal con el rol hospitalario correspondiente</li>
 *   <li>Aplicar RN11: validar unicidad de email y DPI</li>
 *   <li>Crear el registro en usuario_sistema y en personal_hospitalario</li>
 * </ul>
 */
public interface RegisterPersonalUseCase {

    /**
     * Registra un nuevo miembro del personal hospitalario en el sistema.
     *
     * @param request DTO con los datos del personal (nombre, email, password, dpi, direccion, rol)
     * @return AuthResponse con token JWT y datos del usuario creado
     */
    AuthResponse registerPersonal(RegisterPersonalRequest request);
}
