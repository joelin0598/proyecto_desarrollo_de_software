package his.application;

import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.domain.Patient;
import his.domain.TriagePriority;
import his.domain.TriageRecord;
import his.domain.ports.PatientRepository;
import his.domain.ports.TriageRecordRepository;
import his.domain.ports.TriageUseCase;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de triaje (CU-2).
 * Registra signos vitales y calcula la prioridad de atención (RN04).
 *
 * Reglas de clasificación (RN04):
 * RED    – SatO2 &lt; 90 | FC &lt; 40 o &gt; 150 | PAS &lt; 70 | Temp &gt; 40 o &lt; 35
 * ORANGE – SatO2 90–94 | FC 40–50 o 120–150 | Temp 38.5–40 | PAS 70–90
 * GREEN  – todos los valores dentro del rango normal
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TriageService implements TriageUseCase {

    private final TriageRecordRepository triageRecordRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public TriageResponse recordTriage(TriageRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Paciente no encontrado: " + request.getPatientId()));

        TriagePriority priority = calculatePriority(request);
        log.info("Triaje para paciente {} – prioridad calculada: {}", patient.getFullName(), priority);

        TriageRecord record = TriageRecord.builder()
                .patient(patient)
                .systolicPressure(request.getSystolicPressure())
                .diastolicPressure(request.getDiastolicPressure())
                .heartRate(request.getHeartRate())
                .temperature(request.getTemperature())
                .oxygenSaturation(request.getOxygenSaturation())
                .weight(request.getWeight())
                .priority(priority)
                .notes(request.getNotes())
                .build();

        record = triageRecordRepository.save(record);

        auditService.log("RECORD_TRIAGE", "TriageRecord", record.getTriageId(),
                "Triaje registrado para " + patient.getFullName() + " – prioridad: " + priority);

        if (priority == TriagePriority.RED) {
            log.warn("¡CÓDIGO ROJO! Paciente {} requiere atención inmediata.", patient.getFullName());
        }

        return mapToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriageResponse> getTriageHistory(Long patientId) {
        return triageRecordRepository
                .findByPatient_PatientIdOrderByArrivalTimeDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriageResponse> getWaitingList() {
        return triageRecordRepository
                .findAllByOrderByArrivalTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calcula la prioridad de triaje según los signos vitales (RN04).
     */
    private TriagePriority calculatePriority(TriageRequest r) {
        // Código ROJO – riesgo inminente para la vida (FA03)
        if (isRed(r)) {
            return TriagePriority.RED;
        }
        // NARANJA – urgente
        if (isOrange(r)) {
            return TriagePriority.ORANGE;
        }
        // VERDE – no urgente
        return TriagePriority.GREEN;
    }

    private boolean isRed(TriageRequest r) {
        if (r.getOxygenSaturation() != null && r.getOxygenSaturation() < 90) return true;
        if (r.getHeartRate() != null && (r.getHeartRate() < 40 || r.getHeartRate() > 150)) return true;
        if (r.getSystolicPressure() != null && r.getSystolicPressure() < 70) return true;
        if (r.getTemperature() != null && (r.getTemperature() > 40 || r.getTemperature() < 35)) return true;
        return false;
    }

    private boolean isOrange(TriageRequest r) {
        if (r.getOxygenSaturation() != null && r.getOxygenSaturation() < 95) return true;
        if (r.getHeartRate() != null && (r.getHeartRate() < 50 || r.getHeartRate() > 120)) return true;
        if (r.getSystolicPressure() != null && r.getSystolicPressure() < 90) return true;
        if (r.getTemperature() != null && r.getTemperature() >= 38.5) return true;
        return false;
    }

    private TriageResponse mapToResponse(TriageRecord record) {
        return TriageResponse.builder()
                .triageId(record.getTriageId())
                .patient(PatientMapper.toResponse(record.getPatient()))
                .systolicPressure(record.getSystolicPressure())
                .diastolicPressure(record.getDiastolicPressure())
                .heartRate(record.getHeartRate())
                .temperature(record.getTemperature())
                .oxygenSaturation(record.getOxygenSaturation())
                .weight(record.getWeight())
                .priority(record.getPriority())
                .notes(record.getNotes())
                .arrivalTime(record.getArrivalTime())
                .registeredBy(record.getRegisteredBy())
                .build();
    }
}
