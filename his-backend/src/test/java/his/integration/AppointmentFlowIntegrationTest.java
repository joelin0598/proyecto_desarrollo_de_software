package his.integration;

import his.application.dto.AuthResponse;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.services.AppointmentService;
import his.application.services.AuthService;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentFlowIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private PatientJpaRepository patientJpaRepository;
    @Autowired private HospitalStaffJpaRepository hospitalStaffJpaRepository;
    @Autowired private MedicalAppointmentJpaRepository medicalAppointmentJpaRepository;

    @Test
    void scheduleAppointment_withCardPayment_persistsExpectedState() {
        RegisterRequest patientRequest = RegisterRequest.builder()
                .nombreCompleto("Carlos Lopez")
                .email("carlos.appt@example.com")
                .password("Segura1!")
                .dpi("9876543210123")
                .genero(PatientGender.MASCULINO)
                .build();

        RegisterRequestAdmin doctorRequest = RegisterRequestAdmin.builder()
                .nombreCompleto("Dra. Morales")
                .email("doctora.appt@example.com")
                .password("Segura1!")
                .direccion("Zona 10")
                .telefonoCorporativo("50212345678")
                .rol(Role.DOCTOR)
                .numeroColegiado("COL-999")
                .build();

        RegisterRequestAdmin recepcionRequest = RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion Uno")
                .email("recepcion.appt@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50287654321")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-998")
                .build();

        AuthResponse patientAuth = authService.register(patientRequest);
        AuthResponse doctorAuth = authService.registerPersonal(doctorRequest);
        authService.registerPersonal(recepcionRequest);

        Long pacienteId = patientJpaRepository.findByDpi("9876543210123")
                .orElseThrow()
                .getPacienteId();

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow()
                .getPersonalId();

        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(pacienteId)
                .medicoPersonalId(doctorPersonalId)
                .especialidadId(1L)
                .fechaCita(LocalDate.now().plusDays(2))
                .horaCita(LocalTime.of(8, 30))
                .motivoConsulta("Consulta de seguimiento")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta("12/30")
                .nombreTitularTarjeta("CARLOS LOPEZ")
                .cvc("123")
                .build();

        ScheduleAppointmentResponse response = appointmentService.scheduleAppointment(request, "recepcion.appt@example.com");

        assertNotNull(patientAuth.getToken());
        assertNotNull(response.getCitaMedicaId());
        assertEquals(StatusAppointment.PROGRAMADA, response.getEstadoCita());
        assertEquals(AdministrativeAppointmentStatus.PAGO_VALIDADO, response.getEstadoAdministrativo());
        assertEquals(175.0, response.getCostoConsulta());
        assertTrue(medicalAppointmentJpaRepository.count() >= 1);
    }
}
