package his.application.services;

import his.application.dto.TriageListItemsResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import his.domain.models.Priority;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private HospitalStaffRepository hospitalStaffRepository;
    @Mock private UserRepository userRepository;
    @Mock private MedicalAppointmentRepository medicalAppointmentRepository;
    @Mock private PaymentValidationService paymentValidationService;

    private TriageService triageService;

    private static final String EMAIL_PERSONAL = "enfermera@hospital.com";

    @BeforeEach
    void setUp() {
        triageService = new TriageService(
                patientRepository,
                hospitalStaffRepository,
                userRepository,
                medicalAppointmentRepository,
                paymentValidationService);

        lenient().when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubPersonalResolution(Long userId, Long personalId) {
        User user = User.builder().userId(userId).email(EMAIL_PERSONAL).build();
        HospitalStaff staff = HospitalStaff.builder().personalId(personalId).usuarioId(userId).rol(Role.ENFERMERA).build();
        when(userRepository.findByEmail(EMAIL_PERSONAL)).thenReturn(Optional.of(user));
        when(hospitalStaffRepository.findByUsuarioId(userId)).thenReturn(Optional.of(staff));
    }

    private TriageRequest buildValidRequest(String dpi) {
        return TriageRequest.builder()
                .nombreCompleto("Ana Garcia")
                .dpi(dpi)
                .genero(PatientGender.FEMENINO)
                .contactoEmergencia("Pedro Garcia")
                .telefonoEmergencia("55551234")
                .metodoPago(PaymentOption.TARJETA)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(72)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .tallaCm(165)
                .pesoKg(60)
                .build();
    }

    @Test
    void execute_createsWalkInAppointment_whenNoScheduledAppointmentId() {
        stubPersonalResolution(10L, 20L);
        when(patientRepository.findByDpi("9876543210123")).thenReturn(Optional.empty());

        Patient savedPatient = Patient.builder()
                .pacienteId(5L).nombreCompleto("Ana Garcia").dpi("9876543210123").build();
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        MedicalAppointment walkInCreated = MedicalAppointment.builder()
                .citaMedicaId(44L)
                .pacienteId(5L)
                .citaProgramada(false)
                .solvenciaPago(true)
                .build();

        MedicalAppointment walkInWithTriage = MedicalAppointment.builder()
                .citaMedicaId(44L)
                .pacienteId(5L)
                .citaProgramada(false)
                .solvenciaPago(true)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(72)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .tallaCm(165.0)
                .pesoKg(60.0)
                .prioridad(Priority.VERDE)
                .fechaHoraTriaje(LocalDateTime.now())
                .build();

        when(medicalAppointmentRepository.save(any(MedicalAppointment.class)))
                .thenReturn(walkInCreated)
                .thenReturn(walkInWithTriage);

        TriageResponse result = triageService.execute(buildValidRequest("9876543210123"), EMAIL_PERSONAL);

        assertTrue(result.isPacienteNuevo());
        assertEquals(44L, result.getCitaMedicaId());
        assertEquals(Priority.VERDE, result.getPrioridad());
    }

    @Test
    void execute_updatesExistingScheduledAppointment_whenCitaMedicaIdProvided() {
        stubPersonalResolution(10L, 20L);

        Patient existing = Patient.builder()
                .pacienteId(3L).nombreCompleto("Ana Garcia").dpi("9876543210123").build();
        when(patientRepository.findByDpi("9876543210123")).thenReturn(Optional.of(existing));

        MedicalAppointment existingAppointment = MedicalAppointment.builder()
                .citaMedicaId(10L)
                .pacienteId(3L)
                .citaProgramada(true)
                .solvenciaPago(true)
                .build();
        when(medicalAppointmentRepository.findById(10L)).thenReturn(Optional.of(existingAppointment));

        MedicalAppointment updated = MedicalAppointment.builder()
                .citaMedicaId(10L)
                .pacienteId(3L)
                .citaProgramada(true)
                .solvenciaPago(true)
                .prioridad(Priority.NARANJA)
                .fechaHoraTriaje(LocalDateTime.now())
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(72)
                .temperatura(38.7)
                .saturacionOxigeno(90)
                .tallaCm(165.0)
                .pesoKg(60.0)
                .build();
        when(medicalAppointmentRepository.save(any(MedicalAppointment.class))).thenReturn(updated);

        TriageRequest request = buildValidRequest("9876543210123");
        request.setCitaMedicaId(10L);
        request.setTemperatura(38.7);
        request.setSaturacionOxigeno(90);

        TriageResponse response = triageService.execute(request, EMAIL_PERSONAL);

        assertEquals(10L, response.getCitaMedicaId());
        assertEquals(Priority.NARANJA, response.getPrioridad());
        assertTrue(response.isPagoValidado());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void execute_throwsException_whenEmailPersonalNotFound() {
        when(userRepository.findByEmail(EMAIL_PERSONAL)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> triageService.execute(buildValidRequest("9876543210123"), EMAIL_PERSONAL));

        assertTrue(ex.getMessage().contains("usuario autenticado"));
        verify(patientRepository, never()).findByDpi(any());
    }

    @Test
    void listarTriajesRecientes_returnsMappedItemsFromAppointments() {
        MedicalAppointment appointment = MedicalAppointment.builder()
                .citaMedicaId(100L)
                .pacienteId(1L)
                .fechaHoraTriaje(LocalDateTime.now())
                .prioridad(Priority.VERDE)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(70)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .pesoKg(65.0)
                .tallaCm(170.0)
                .build();

        when(medicalAppointmentRepository.findAllOrderByDateTimeDesc()).thenReturn(List.of(appointment));

        Patient patient = Patient.builder()
                .pacienteId(1L)
                .nombreCompleto("Ana Garcia")
                .dpi("1234567890123")
                .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        List<TriageListItemsResponse> result = triageService.listarTriajesRecientes();

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getSignosVitalesId());
        assertEquals("Ana Garcia", result.get(0).getNombreCompleto());
        assertFalse(result.get(0).isAlertaEmergencia());
        assertNotNull(result.get(0).getFechaHoraRegistro());
    }


}
