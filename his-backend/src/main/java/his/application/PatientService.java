package his.application;

import his.adapters.exception.DuplicateDpiException;
import his.adapters.exception.PatientNotFoundException;
import his.application.dto.PatientRequest;
import his.application.dto.PatientResponse;
import his.infrastructure.persistence.PatientEntity;
import his.infrastructure.persistence.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientJpaRepository patientJpaRepository;

    public PatientResponse createPatient(PatientRequest request) {
        log.info("Registrando paciente con DPI: {}", request.getDpi());

        if (patientJpaRepository.existsByDpi(request.getDpi())) {
            throw new DuplicateDpiException("Ya existe un paciente registrado con el DPI: " + request.getDpi());
        }

        PatientEntity patient = PatientEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dpi(request.getDpi())
                .fechaNacimiento(request.getFechaNacimiento())
                .genero(request.getGenero())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .email(request.getEmail())
                .build();

        PatientEntity saved = patientJpaRepository.save(patient);
        log.info("Paciente registrado exitosamente con ID: {}", saved.getId());

        return toResponse(saved);
    }

    public List<PatientResponse> getAllPatients() {
        return patientJpaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PatientResponse getPatientById(Long id) {
        PatientEntity patient = patientJpaRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con ID: " + id));
        return toResponse(patient);
    }

    private PatientResponse toResponse(PatientEntity entity) {
        return PatientResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dpi(entity.getDpi())
                .fechaNacimiento(entity.getFechaNacimiento())
                .genero(entity.getGenero())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .email(entity.getEmail())
                .build();
    }
}
