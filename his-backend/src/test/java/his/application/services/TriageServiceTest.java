package his.application.services;

import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.domain.models.HospitalStaff;
import his.domain.models.Patient;
import his.domain.models.PatientGender;
import his.domain.models.Priority;
import his.domain.models.User;
import his.domain.models.VitalSigns;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import his.domain.ports.VitalSignsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private VitalSignsRepository vitalSignsRepository;
    @Mock private HospitalStaffRepository hospitalStaffRepository;
    @Mock private UserRepository userRepository;

    private TriageService triageService;

    private static final String EMAIL_PERSONAL = "enfermera@hospital.com";

    @BeforeEach
    void setUp() {
        triageService = new TriageService(patientRepository, vitalSignsRepository, hospitalStaffRepository, userRepository);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers de stub reutilizables
    // ──────────────────────────────────────────────────────────────────────────

    private void stubPersonalResolution(Long userId, Long personalId) {
        User user = User.builder().userId(userId).email(EMAIL_PERSONAL).build();
        HospitalStaff staff = HospitalStaff.builder().personalId(personalId).usuarioId(userId).build();
        when(userRepository.findByEmail(EMAIL_PERSONAL)).thenReturn(Optional.of(user));
        when(hospitalStaffRepository.findByUsuarioId(userId)).thenReturn(Optional.of(staff));
    }

    private TriageRequest buildValidRequest(String dpi) {
        return TriageRequest.builder()
                .nombreCompleto("Ana García")
                .dpi(dpi)
                .genero(PatientGender.FEMENINO)
                .contactoEmergencia("Pedro García")
                .telefonoEmergencia("55551234")
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(72)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .tallaCm(165)
                .pesoKg(60)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FA01: paciente nuevo (DPI no existe en BD)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void execute_createsNewPatient_whenDpiNotFound() {
        // Arrange
        stubPersonalResolution(10L, 20L);
        when(patientRepository.findByDpi("9876543210123")).thenReturn(Optional.empty());

        Patient savedPatient = Patient.builder()
                .pacienteId(5L).nombreCompleto("Ana García").dpi("9876543210123").build();
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        VitalSigns savedVitals = VitalSigns.builder()
                .signosVitalesId(99L).pacienteId(5L).personalId(20L)
                .presionSistolica(120).presionDiastolica(80).frecuenciaCardiaca(72)
                .temperatura(36.8).saturacionOxigeno(98).pesoKg(60).tallaCm(165)
                .build();
        savedVitals.calculatePriority();
        when(vitalSignsRepository.save(any(VitalSigns.class))).thenReturn(savedVitals);

        // Act
        TriageResponse result = triageService.execute(buildValidRequest("9876543210123"), EMAIL_PERSONAL);

        // Assert
        assertTrue(result.isPacienteNuevo(), "Debe marcarse como paciente nuevo (FA01)");
        assertEquals(5L, result.getPacienteId());
        assertEquals(99L, result.getSignosVitalesId());
        verify(patientRepository).save(any(Patient.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FA01: paciente existente (DPI ya registrado)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void execute_reusesExistingPatient_whenDpiFound() {
        // Arrange
        stubPersonalResolution(10L, 20L);

        Patient existing = Patient.builder()
                .pacienteId(3L).nombreCompleto("Ana García").dpi("9876543210123").build();
        when(patientRepository.findByDpi("9876543210123")).thenReturn(Optional.of(existing));

        VitalSigns savedVitals = VitalSigns.builder()
                .signosVitalesId(77L).pacienteId(3L).personalId(20L)
                .presionSistolica(120).presionDiastolica(80).frecuenciaCardiaca(72)
                .temperatura(36.8).saturacionOxigeno(98).pesoKg(60).tallaCm(165)
                .build();
        savedVitals.calculatePriority();
        when(vitalSignsRepository.save(any(VitalSigns.class))).thenReturn(savedVitals);

        // Act
        TriageResponse result = triageService.execute(buildValidRequest("9876543210123"), EMAIL_PERSONAL);

        // Assert
        assertFalse(result.isPacienteNuevo(), "No debe marcarse como nuevo si el DPI ya existe");
        assertEquals(3L, result.getPacienteId());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RN04 + FA03: signos críticos → prioridad ROJO → alerta de emergencia
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void execute_triggersRedAlert_whenVitalSignsAreCritical() {
        // Arrange
        stubPersonalResolution(10L, 20L);

        Patient existing = Patient.builder()
                .pacienteId(1L).nombreCompleto("Carlos López").dpi("1111111111111").build();
        when(patientRepository.findByDpi("1111111111111")).thenReturn(Optional.of(existing));

        // Signos críticos → temperatura 40.1 °C + saturación 83% → ROJO
        TriageRequest criticalRequest = TriageRequest.builder()
                .nombreCompleto("Carlos López")
                .dpi("1111111111111")
                .genero(PatientGender.MASCULINO)
                .contactoEmergencia("María López")
                .telefonoEmergencia("55559999")
                .presionSistolica(120).presionDiastolica(80)
                .frecuenciaCardiaca(76).temperatura(40.1)
                .saturacionOxigeno(83).tallaCm(170).pesoKg(70)
                .build();

        ArgumentCaptor<VitalSigns> captor = ArgumentCaptor.forClass(VitalSigns.class);
        when(vitalSignsRepository.save(captor.capture())).thenAnswer(inv -> {
            VitalSigns v = inv.getArgument(0);
            v.setSignosVitalesId(50L);
            return v;
        });

        // Act
        TriageResponse result = triageService.execute(criticalRequest, EMAIL_PERSONAL);

        // Assert
        assertEquals(Priority.ROJO, result.getPrioridad(), "RN04: signos críticos deben dar prioridad ROJO");
        assertTrue(result.isAlertaEmergencia(), "FA03: debe activar alerta de emergencia cuando prioridad es ROJO");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Error: email de personal no encontrado en BD
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void execute_throwsException_whenEmailPersonalNotFound() {
        when(userRepository.findByEmail(EMAIL_PERSONAL)).thenReturn(Optional.empty());

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> triageService.execute(buildValidRequest("9876543210123"), EMAIL_PERSONAL));

        assertTrue(ex.getMessage().contains("No se encontró el usuario autenticado"));
        verify(patientRepository, never()).findByDpi(any());
        verify(vitalSignsRepository, never()).save(any());
    }
}
