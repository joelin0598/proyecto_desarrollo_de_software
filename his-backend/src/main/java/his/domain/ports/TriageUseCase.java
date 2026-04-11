package his.domain.ports;

import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;

import java.util.List;

/**
 * Puerto de aplicación para el triaje de pacientes (CU-2).
 */
public interface TriageUseCase {

    TriageResponse recordTriage(TriageRequest request);

    List<TriageResponse> getTriageHistory(Long patientId);

    List<TriageResponse> getWaitingList();
}
