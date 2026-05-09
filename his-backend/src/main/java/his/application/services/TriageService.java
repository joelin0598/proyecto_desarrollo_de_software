package his.application.services;

import his.application.dto.TriageRequest;
import his.application.usecases.TriageUseCase;
import his.domain.models.VitalSigns;
import his.domain.ports.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TriageService implements TriageUseCase {

    private static final int MIN_SATURACION = 50;
    private static final int MAX_SATURACION = 100;
    private static final double MIN_TEMPERATURA = 30.0;
    private static final double MAX_TEMPERATURA = 45.0;
    private static final int MIN_PRESION_SISTOLICA = 50;
    private static final int MAX_PRESION_SISTOLICA = 300;
    private static final int MIN_PRESION_DIASTOLICA = 30;
    private static final int MAX_PRESION_DIASTOLICA = 200;
    private static final int MIN_FRECUENCIA_CARDIACA = 20;
    private static final int MAX_FRECUENCIA_CARDIACA = 250;
    private static final double MIN_TALLA_CM = 30.0;
    private static final double MAX_TALLA_CM = 300.0;
    private static final double MIN_PESO_KG = 1.0;
    private static final double MAX_PESO_KG = 500.0;

    private final VitalSignsRepository vitalSignsRepository;

    @Transactional
    @Override
    public VitalSigns execute(TriageRequest request) {
        validateRequest(request);

        VitalSigns vitalSigns = VitalSigns.builder()
                .pacienteId(request.getPacienteId())
                .personalId(request.getPersonalId())
                .citaMedicaId(request.getCitaMedicaId())
                .presionSistolica(request.getPresionSistolica())
                .presionDiastolica(request.getPresionDiastolica())
                .frecuenciaCardiaca(request.getFrecuenciaCardiaca())
                .temperatura(request.getTemperatura())
                .saturacionOxigeno(request.getSaturacionOxigeno())
                .tallaCm(request.getTallaCm())
                .pesoKg(request.getPesoKg())
                .build();

        vitalSigns.calculatePriority();
        return vitalSignsRepository.save(vitalSigns);
    }

    private void validateRequest(TriageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de triaje es obligatoria");
        }
        if (request.getPacienteId() == null || request.getPacienteId() <= 0) {
            throw new IllegalArgumentException("El pacienteId es obligatorio y debe ser mayor que cero");
        }
        if (request.getPersonalId() == null || request.getPersonalId() <= 0) {
            throw new IllegalArgumentException("El personalId es obligatorio y debe ser mayor que cero");
        }
        if (request.getCitaMedicaId() != null && request.getCitaMedicaId() <= 0) {
            throw new IllegalArgumentException("El citaMedicaId, cuando se envía, debe ser mayor que cero");
        }

        validateRange(request.getPresionSistolica(), MIN_PRESION_SISTOLICA, MAX_PRESION_SISTOLICA,
                "La presión sistólica está fuera de rango clínico");
        validateRange(request.getPresionDiastolica(), MIN_PRESION_DIASTOLICA, MAX_PRESION_DIASTOLICA,
                "La presión diastólica está fuera de rango clínico");

        if (request.getPresionDiastolica() >= request.getPresionSistolica()) {
            throw new IllegalArgumentException("La presión diastólica no puede ser mayor o igual a la sistólica");
        }

        validateRange(request.getFrecuenciaCardiaca(), MIN_FRECUENCIA_CARDIACA, MAX_FRECUENCIA_CARDIACA,
                "La frecuencia cardíaca está fuera de rango clínico");
        validateRange(request.getSaturacionOxigeno(), MIN_SATURACION, MAX_SATURACION,
                "La saturación de oxígeno está fuera de rango clínico");
        validateRange(request.getTemperatura(), MIN_TEMPERATURA, MAX_TEMPERATURA,
                "La temperatura está fuera de rango clínico");
        validateRange(request.getTallaCm(), MIN_TALLA_CM, MAX_TALLA_CM,
                "La talla en centímetros está fuera de rango clínico");
        validateRange(request.getPesoKg(), MIN_PESO_KG, MAX_PESO_KG,
                "El peso en kilogramos está fuera de rango clínico");
    }

    private void validateRange(double value, double min, double max, String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
    }
}
