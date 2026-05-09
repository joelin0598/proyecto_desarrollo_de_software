package his.integration;

import his.application.dto.AuthResponse;
import his.application.dto.RegisterRequest;
import his.application.dto.TriageRequest;
import his.application.services.AuthService;
import his.application.services.TriageService;
import his.domain.models.Priority;
import his.domain.models.VitalSigns;
import his.infrastructure.persistence.entities.PatientJpaEntity;
import his.infrastructure.persistence.entities.VitalSignsJpaEntity;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import his.infrastructure.persistence.repositories.VitalSignsJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PatientTriageFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private PatientJpaRepository patientJpaRepository;

    @Autowired
    private VitalSignsJpaRepository vitalSignsJpaRepository;

    @Test
    void registerPatientAndCreateTriage_persistsExpectedData() {
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Carlos")
                .lastName("Lopez")
                .email("carlos.lopez@example.com")
                .password("Segura1!")
                .build();

        // Act
        AuthResponse authResponse = authService.register(registerRequest);

        assertNotNull(authResponse);
        assertNotNull(authResponse.getToken());
        assertNotNull(authResponse.getUser());
        assertNotNull(authResponse.getUser().getId());

        PatientJpaEntity patient = patientJpaRepository
                .findByUsuarioSistemaUsuarioId(authResponse.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("No se encontró el paciente creado en registro"));

        TriageRequest triageRequest = TriageRequest.builder()
                .pacienteId(patient.getPacienteId())
                .personalId(200L)
                .citaMedicaId(300L)
                .presionSistolica(118)
                .presionDiastolica(76)
                .frecuenciaCardiaca(82)
                .temperatura(38.7)
                .saturacionOxigeno(94)
                .tallaCm(168)
                .pesoKg(72)
                .build();

        VitalSigns triage = triageService.execute(triageRequest);

        // Assert
        assertNotNull(triage.getSignosVitalesId());
        assertEquals(patient.getPacienteId(), triage.getPacienteId());
        assertEquals(200L, triage.getPersonalId());
        assertEquals(300L, triage.getCitaMedicaId());
        assertEquals(Priority.NARANJA, triage.getPriority());

        VitalSignsJpaEntity persisted = vitalSignsJpaRepository.findById(triage.getSignosVitalesId())
                .orElseThrow(() -> new IllegalStateException("No se encontró el triaje persistido"));

        assertEquals(patient.getPacienteId(), persisted.getPacienteId());
        assertEquals(200L, persisted.getPersonalId());
        assertEquals(Priority.NARANJA, persisted.getPriority());
        assertTrue(patientJpaRepository.count() >= 1);
        assertTrue(vitalSignsJpaRepository.count() >= 1);
    }
}


