package his.domain.ports;

import his.application.dto.AuthResponse;
import his.application.dto.RegisterPacienteRequest;

/**
 * Puerto de entrada (caso de uso) para el registro de pacientes externos.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Registrar nuevos pacientes con rol PACIENTE</li>
 *   <li>Aplicar RN11: validar unicidad de email y DPI</li>
 *   <li>Crear el registro en usuario_sistema y en paciente</li>
 * </ul>
 */
public interface RegisterPacienteUseCase {

    /**
     * Registra un nuevo paciente en el sistema.
     *
     * @param request DTO con los datos del paciente (nombre, email, password, dpi)
     * @return AuthResponse con token JWT y datos del usuario creado
     */
    AuthResponse registerPaciente(RegisterPacienteRequest request);
}
