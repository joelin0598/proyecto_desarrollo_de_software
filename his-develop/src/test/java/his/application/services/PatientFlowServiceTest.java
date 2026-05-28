package his.application.services;

import his.application.dto.PatientRegisterRequest;
import his.application.dto.PatientTriageRequest;
import his.application.dto.TriageRequest;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientFlowServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private MedicalAppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private HospitalStaffRepository staffRepository;
    @Mock private PaymentValidationService paymentValidationService;

    private PatientFlowService service;

    @BeforeEach
    void setUp() {
        service = new PatientFlowService(
                patientRepository,
                appointmentRepository,
                userRepository,
                staffRepository,
                paymentValidationService);
    }

    @Test
    void register_throwsWhenNoPaymentMethod_FA05() {
        PatientRegisterRequest req = new PatientRegisterRequest();
        req.setNombreCompleto("Ana Torres");
        req.setDpi("1234567890123");
        req.setGenero(PatientGender.FEMENINO);
        req.setContactoEmergencia("Pedro Torres");
        req.setTelefonoEmergencia("55551234");

        mockStaff();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(req, "recepcion@hospital.com"));
        assertEquals("FA05: Debe registrar un metodo de pago para continuar.", ex.getMessage());
    }

    @Test
    void register_createsPatientAndAppointment_whenPaymentApproved() {
        PatientRegisterRequest req = new PatientRegisterRequest();
        req.setNombreCompleto("Ana Torres");
        req.setDpi("1234567890123");
        req.setGenero(PatientGender.FEMENINO);
        req.setContactoEmergencia("Pedro Torres");
        req.setTelefonoEmergencia("55551234");
        req.setMetodoPago(PaymentOption.TARJETA);
        req.setBancoTarjeta("Banco Demo");
        req.setNumeroTarjeta("4111111111111111");
        req.setFechaVencimientoTarjeta("12/30");
        req.setNombreTitularTarjeta("Ana Torres");
        req.setCvc("123");

        mockStaff();
        when(paymentValidationService.validateForTriage(any(TriageRequest.class)))
                .thenReturn(new PaymentValidationService.PaymentValidationResult(true, "OK"));
        when(patientRepository.save(any(Patient.class))).thenReturn(Patient.builder().pacienteId(10L).dpi("1234567890123").nombreCompleto("Ana Torres").build());
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenReturn(MedicalAppointment.builder().citaMedicaId(20L).build());

        var resp = service.register(req, "recepcion@hospital.com");
        assertNotNull(resp);
        assertEquals(10L, resp.getPacienteId());
        assertEquals(20L, resp.getCitaMedicaId());
        verify(appointmentRepository).save(argThat(cita ->
                Long.valueOf(2L).equals(cita.getPersonalId())
                        && Boolean.FALSE.equals(cita.getCitaProgramada())
                        && Boolean.TRUE.equals(cita.getSolvenciaPago())));
    }

    @Test
    void register_throwsWhenDpiAlreadyExists() {
        PatientRegisterRequest req = new PatientRegisterRequest();
        req.setNombreCompleto("Ana Torres");
        req.setDpi("1234567890123");
        req.setGenero(PatientGender.FEMENINO);
        req.setContactoEmergencia("Pedro Torres");
        req.setTelefonoEmergencia("55551234");
        req.setMetodoPago(PaymentOption.TARJETA);

        mockStaff();
        when(patientRepository.existsByDpi("1234567890123")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(req, "recepcion@hospital.com"));
        assertEquals("El DPI ya fue registrado previamente.", ex.getMessage());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        PatientRegisterRequest req = new PatientRegisterRequest();
        req.setNombreCompleto("Ana Torres");
        req.setDpi("1234567890123");
        req.setGenero(PatientGender.FEMENINO);
        req.setContactoEmergencia("Pedro Torres");
        req.setTelefonoEmergencia("55551234");
        req.setMetodoPago(PaymentOption.TARJETA);
        req.setEmailContacto("ana@email.com");

        mockStaff();
        when(patientRepository.existsByDpi("1234567890123")).thenReturn(false);
        when(patientRepository.existsByEmailContacto("ana@email.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(req, "recepcion@hospital.com"));
        assertEquals("El correo ya fue registrado previamente.", ex.getMessage());
    }

    @Test
    void triage_updatesAppointment_whenPaid() {
        mockStaff();

        PatientTriageRequest req = new PatientTriageRequest();
        req.setCitaMedicaId(50L);
        req.setPresionSistolica(120);
        req.setPresionDiastolica(80);
        req.setFrecuenciaCardiaca(70);
        req.setTemperatura(36.9);
        req.setSaturacionOxigeno(98);
        req.setPesoKg(70);
        req.setTallaCm(170);

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(
                MedicalAppointment.builder()
                        .citaMedicaId(50L)
                        .pacienteId(5L)
                        .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                        .solvenciaPago(true)
                        .build()
        ));
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenAnswer(i -> i.getArgument(0));
        when(patientRepository.findById(5L)).thenReturn(Optional.of(
                Patient.builder().pacienteId(5L).nombreCompleto("Ana").dpi("1234567890123").build()
        ));

        var resp = service.triage(req, "recepcion@hospital.com");
        assertEquals(50L, resp.getCitaMedicaId());
        assertEquals(120, resp.getPresionSistolica());
    }

    private void mockStaff() {
        when(userRepository.findByEmail("recepcion@hospital.com"))
                .thenReturn(Optional.of(User.builder().userId(1L).email("recepcion@hospital.com").build()));
        when(staffRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(HospitalStaff.builder().personalId(2L).usuarioId(1L).rol(Role.RECEPCION).build()));
    }
}

