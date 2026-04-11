package his.application;

import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;
import his.domain.PatientEntity;
import his.infrastructure.persistence.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un paciente con el email proporcionado");
        }

        if (patientRepository.findByDpi(request.getDpi()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un paciente con el DPI proporcionado");
        }

        PatientEntity patient = PatientEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(LocalDate.parse(request.getDateOfBirth()))
                .gender(request.getGender())
                .address(request.getAddress())
                .dpi(request.getDpi())
                .emergencyPhone(request.getEmergencyPhone())
                .build();

        patient = patientRepository.save(patient);
        log.info("Paciente creado exitosamente - ID: {}", patient.getPatientId());
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + id));
        return mapToResponse(patient);
    }

    private PatientResponse mapToResponse(PatientEntity patient) {
        return PatientResponse.builder()
                .id(patient.getPatientId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null)
                .gender(patient.getGender())
                .address(patient.getAddress())
                .dpi(patient.getDpi())
                .emergencyPhone(patient.getEmergencyPhone())
                .build();
    }
}
