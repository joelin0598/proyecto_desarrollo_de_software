package his.application.usecases;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;

import java.util.List;

/**
 * CU06 — Atención médica sobre citas: cola de espera, apertura y cierre de atención.
 */
public interface MedicalAppointmentAttentionUseCase {

    /** RN09 / FA02 — Cola de espera del medico autenticado. */
    List<MedicalAppointmentQueueItemResponse> getPatientQueue(String emailDoctor);

    /** Flujo 1-3: el medico solicita siguiente paciente y abre la consulta. */
    MedicalAppointmentAttentionResponse openAttention(Long citaMedicaId, String emailDoctor);

    /** Flujo 4-5 + RN13: el medico cierra la consulta con registro clinico completo. */
    MedicalAppointmentAttentionResponse closeAttention(CloseMedicalAppointmentAttentionRequest request, String emailDoctor);

    /** Cancela la atencion en curso sin marcar ATENDIDA ni persistir detalle clinico final. */
    boolean cancelCurrentAttention(String emailDoctor);

    /** Retorna la consulta EN_CURSO del medico autenticado, si existe. */
    MedicalAppointmentAttentionResponse getCurrentAttention(String emailDoctor);
}

