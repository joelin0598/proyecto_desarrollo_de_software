package his.integration;

import his.application.dto.AuthResponse;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.RegisterRequest;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.application.services.AuthService;
import his.application.services.TriageService;
import his.domain.models.Priority;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import his.infrastructure.persistence.entities.MedicalAppointmentJpaEntity;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PatientTriageFlowIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private TriageService triageService;
    @Autowired private PatientJpaRepository patientJpaRepository;
    @Autowired private HospitalStaffJpaRepository hospitalStaffJpaRepository;
    @Autowired private MedicalAppointmentJpaRepository medicalAppointmentJpaRepository;

    @Test
    void registerPatientAndCreateTriage_persistsExpectedData() {
        // Arrange: registrar paciente web y personal hospitalario
        RegisterRequest registerRequest = RegisterRequest.builder()
                .nombreCompleto("Carlos Lopez")
                .email("carlos.lopez@example.com")
                .password("Segura1!")
                .dpi("1234567890123")
                .genero(PatientGender.MASCULINO)
                .build();

        RegisterRequestAdmin registerPersonalRequest = RegisterRequestAdmin.builder()
                .nombreCompleto("Nora Enfermera")
                .email("nora.enfermera@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50212345678")
                .rol(Role.ENFERMERA)
                .numeroColegiado("COL-123")
                .build();

        AuthResponse authResponse = authService.register(registerRequest);
        AuthResponse personalResponse = authService.registerPersonal(registerPersonalRequest);

        assertNotNull(authResponse.getToken());
        assertNotNull(personalResponse.getUser().getId());

        // El personal se identifica por su email (extraído del JWT en prod; pasado directo aquí)
        String emailPersonal = "nora.enfermera@example.com";

        HospitalStaffJpaEntity personal = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(personalResponse.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("No se encontró el personal hospitalario"));

        // Act: triaje del paciente ya registrado (encontrado por DPI → FA01 = existente)
        TriageRequest triageRequest = TriageRequest.builder()
                .nombreCompleto("Carlos Lopez")
                .dpi("1234567890123")             // mismo DPI del registro web → paciente existente
                .genero(PatientGender.MASCULINO)
                .contactoEmergencia("Maria Lopez")
                .telefonoEmergencia("55551234")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta("12/99")
                .nombreTitularTarjeta("CARLOS LOPEZ")
                .cvc("123")
                .presionSistolica(118)
                .presionDiastolica(76)
                .frecuenciaCardiaca(82)
                .temperatura(38.7)
                .saturacionOxigeno(94)
                .tallaCm(168)
                .pesoKg(72)
                .build();

        TriageResponse triage = triageService.execute(triageRequest, emailPersonal);

        // Assert
        assertNotNull(triage.getSignosVitalesId());
        assertFalse(triage.isPacienteNuevo(), "El paciente ya existe (registrado vía web) → FA01 reutiliza expediente");
        assertEquals("1234567890123", triage.getDpi());
        assertEquals(Priority.NARANJA, triage.getPrioridad());
        assertFalse(triage.isAlertaEmergencia());
        MedicalAppointmentJpaEntity cita = medicalAppointmentJpaRepository.findById(triage.getCitaMedicaId())
                .orElseThrow(() -> new IllegalStateException("No se encontro cita consolidada de triaje"));
        assertEquals(triage.getPresionSistolica(), cita.getPresionSistolica());
        assertEquals(triage.getPresionDiastolica(), cita.getPresionDiastolica());
        assertEquals(triage.getFrecuenciaCardiaca(), cita.getFrecuenciaCardiaca());
        assertEquals(personal.getPersonalId(), hospitalStaffJpaRepository.findById(personal.getPersonalId()).orElseThrow().getPersonalId());
        assertTrue(patientJpaRepository.count() >= 1);
        assertTrue(medicalAppointmentJpaRepository.count() >= 1);
    }
}
