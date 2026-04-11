package his.application;

import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;
import his.domain.Patient;
import his.domain.ports.PatientRepository;
import his.domain.ports.PatientUseCase;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para el registro y gestión de pacientes (CU-2).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService implements PatientUseCase {

    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public PatientResponse registerPatient(PatientRequest request) {
        log.info("Registrando paciente: {}", request.getFullName());

        if (request.getDpi() != null && !request.getDpi().isBlank()
                && patientRepository.existsByDpi(request.getDpi())) {
            throw new IllegalArgumentException("Ya existe un paciente con el DPI: " + request.getDpi());
        }

        Patient patient = Patient.builder()
                .fullName(request.getFullName())
                .dpi(request.getDpi())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .insuranceProvider(request.getInsuranceProvider())
                .build();

        patient = patientRepository.save(patient);
        auditService.log("REGISTER_PATIENT", "Patient", patient.getPatientId(),
                "Nuevo paciente registrado: " + patient.getFullName());

        return mapToResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(Long patientId, PatientRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado: " + patientId));

        patient.setFullName(request.getFullName());
        patient.setDpi(request.getDpi());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        patient.setInsuranceProvider(request.getInsuranceProvider());

        patient = patientRepository.save(patient);
        auditService.log("UPDATE_PATIENT", "Patient", patient.getPatientId(),
                "Paciente actualizado: " + patient.getFullName());

        return mapToResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado: " + patientId));
        return mapToResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientResponse> findByDpi(String dpi) {
        return patientRepository.findByDpi(dpi).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientMapper.toResponse(patient);
    }
}
