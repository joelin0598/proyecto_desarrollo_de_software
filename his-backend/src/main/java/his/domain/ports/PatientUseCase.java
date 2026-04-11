package his.domain.ports;

import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de aplicación para la gestión de pacientes (CU-2).
 */
public interface PatientUseCase {

    PatientResponse registerPatient(PatientRequest request);

    PatientResponse updatePatient(Long patientId, PatientRequest request);

    PatientResponse getPatientById(Long patientId);

    Optional<PatientResponse> findByDpi(String dpi);

    List<PatientResponse> getAllPatients();
}
