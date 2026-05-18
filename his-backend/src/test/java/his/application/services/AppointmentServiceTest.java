package his.application.services;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.InsuranceCatalogRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private MedicalAppointmentRepository medicalAppointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HospitalStaffRepository hospitalStaffRepository;
    @Mock private InsuranceCatalogRepository insuranceCatalogRepository;
    @Mock private UserRepository userRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                medicalAppointmentRepository,
                patientRepository,
                hospitalStaffRepository,
                insuranceCatalogRepository,
                userRepository
        );
    }

    @Test
    void scheduleAppointment_cardApproved_setsPagoValidado() {
        String email = "recepcion@hospital.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().userId(1L).role(Role.RECEPCION).build()));
        when(patientRepository.findById(20L)).thenReturn(Optional.of(Patient.builder().pacienteId(20L).dpi("1234567890123").build()));
        when(hospitalStaffRepository.findById(30L)).thenReturn(Optional.of(HospitalStaff.builder().personalId(30L).rol(Role.DOCTOR).build()));
        when(medicalAppointmentRepository.existsByPersonalIdAndDateTime(30L, LocalDate.now().plusDays(2), LocalTime.of(8, 0)))
                .thenReturn(false);

        when(medicalAppointmentRepository.save(any(MedicalAppointment.class))).thenAnswer(invocation -> {
            MedicalAppointment m = invocation.getArgument(0);
            m.setCitaMedicaId(999L);
            return m;
        });

        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(20L)
                .medicoPersonalId(30L)
                .especialidadId(5L)
                .fechaCita(LocalDate.now().plusDays(2))
                .horaCita(LocalTime.of(8, 0))
                .motivoConsulta("Control general")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta(nextMonth())
                .nombreTitularTarjeta("JUAN PEREZ")
                .cvc("123")
                .build();

        ScheduleAppointmentResponse response = appointmentService.scheduleAppointment(request, email);

        assertEquals(999L, response.getCitaMedicaId());
        assertEquals(175.0, response.getCostoConsulta());
        assertEquals(StatusAppointment.PROGRAMADA, response.getEstadoCita());
        assertEquals(AdministrativeAppointmentStatus.PAGO_VALIDADO, response.getEstadoAdministrativo());
        assertTrue(response.isPagoValidado());
    }

    @Test
    void scheduleAppointment_cardRejected_setsPagoPendiente() {
        String email = "recepcion@hospital.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().userId(1L).role(Role.RECEPCION).build()));
        when(patientRepository.findById(20L)).thenReturn(Optional.of(Patient.builder().pacienteId(20L).dpi("1234567890123").build()));
        when(hospitalStaffRepository.findById(30L)).thenReturn(Optional.of(HospitalStaff.builder().personalId(30L).rol(Role.DOCTOR).build()));
        when(medicalAppointmentRepository.existsByPersonalIdAndDateTime(30L, LocalDate.now().plusDays(2), LocalTime.of(8, 30)))
                .thenReturn(false);

        when(medicalAppointmentRepository.save(any(MedicalAppointment.class))).thenAnswer(invocation -> {
            MedicalAppointment m = invocation.getArgument(0);
            m.setCitaMedicaId(1000L);
            return m;
        });

        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(20L)
                .medicoPersonalId(30L)
                .fechaCita(LocalDate.now().plusDays(2))
                .horaCita(LocalTime.of(8, 30))
                .motivoConsulta("Control general")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111110000")
                .fechaVencimientoTarjeta(nextMonth())
                .nombreTitularTarjeta("JUAN PEREZ")
                .cvc("123")
                .build();

        ScheduleAppointmentResponse response = appointmentService.scheduleAppointment(request, email);

        assertEquals(AdministrativeAppointmentStatus.PAGO_PENDIENTE, response.getEstadoAdministrativo());
        assertFalse(response.isPagoValidado());
    }

    @Test
    void scheduleAppointment_rejectsWindowBefore24Hours() {
        String email = "recepcion@hospital.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().userId(1L).role(Role.RECEPCION).build()));
        when(patientRepository.findById(20L)).thenReturn(Optional.of(Patient.builder().pacienteId(20L).dpi("1234567890123").build()));
        when(hospitalStaffRepository.findById(30L)).thenReturn(Optional.of(HospitalStaff.builder().personalId(30L).rol(Role.DOCTOR).build()));

        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(20L)
                .medicoPersonalId(30L)
                .fechaCita(LocalDate.now())
                .horaCita(LocalTime.now().plusMinutes(30).withSecond(0).withNano(0))
                .motivoConsulta("Control general")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta(nextMonth())
                .nombreTitularTarjeta("JUAN PEREZ")
                .cvc("123")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(request, email));

        assertTrue(ex.getMessage().contains("24 horas"));
        verify(medicalAppointmentRepository, never()).save(any());
    }

    @Test
    void scheduleAppointment_blocksNonDoctorAsMedico() {
        String email = "admin@hospital.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().userId(1L).role(Role.ADMIN).build()));
        when(patientRepository.findById(20L)).thenReturn(Optional.of(Patient.builder().pacienteId(20L).dpi("1234567890123").build()));
        when(hospitalStaffRepository.findById(30L)).thenReturn(Optional.of(HospitalStaff.builder().personalId(30L).rol(Role.RECEPCION).build()));

        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(20L)
                .medicoPersonalId(30L)
                .fechaCita(LocalDate.now().plusDays(2))
                .horaCita(LocalTime.of(10, 0))
                .motivoConsulta("Control general")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco Demo")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta(nextMonth())
                .nombreTitularTarjeta("JUAN PEREZ")
                .cvc("123")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(request, email));

        assertTrue(ex.getMessage().contains("rol DOCTOR"));
    }

    private String nextMonth() {
        YearMonth ym = YearMonth.now().plusMonths(1);
        int yy = ym.getYear() % 100;
        return String.format("%02d/%02d", ym.getMonthValue(), yy);
    }
}
