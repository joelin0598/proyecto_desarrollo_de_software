package his.application.services;
import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.LaboratoryOrder;
import his.domain.models.LaboratoryOrderStatus;
import his.domain.models.LaboratoryResult;
import his.application.dto.LaboratoryPaymentRequest;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.LaboratoryOrderRepository;
import his.domain.ports.LaboratoryResultRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class LaboratoryServiceTest {
    @Mock
    private LaboratoryOrderRepository orderRepository;
    @Mock
    private LaboratoryResultRepository resultRepository;
    @Mock
    private MedicalAppointmentDetailsRepository detailsRepository;
    @Mock
    private MedicalAppointmentRepository appointmentRepository;
    @Mock
    private HospitalStaffRepository staffRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    private LaboratoryService service;
    @BeforeEach
    void setUp() {
        service = new LaboratoryService(
                orderRepository,
                resultRepository,
                detailsRepository,
                appointmentRepository,
                staffRepository,
                patientRepository,
                userRepository
        );
    }
    @Test
    void createOrder_fromDoctorStartsWithPendingPayment() {
        CreateLaboratoryOrderRequest request = new CreateLaboratoryOrderRequest();
        request.setCitaMedicaDetalleId(10L);
        request.setNombreExamen("Hemograma");
        request.setTipoMuestra("Sangre");

        when(userRepository.findByEmail("doctor@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.DOCTOR).personalId(99L).build()));
        when(detailsRepository.findById(10L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .build()));
        when(orderRepository.save(any(LaboratoryOrder.class))).thenAnswer(invocation -> {
            LaboratoryOrder input = invocation.getArgument(0);
            input.setOrdenLaboratorioId(123L);
            return input;
        });

        var response = service.createOrder(request, "doctor@hospital.com");

        assertEquals(LaboratoryOrderStatus.PENDIENTE_PAGO, response.getEstado());
        assertFalse(response.isPagoValidado());
    }

    @Test
    void createOrder_throwsWhenAppointmentIsNotSolvent() {
        CreateLaboratoryOrderRequest request = new CreateLaboratoryOrderRequest();
        request.setCitaMedicaDetalleId(10L);
        request.setNombreExamen("Hemograma");
        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));
        when(detailsRepository.findById(10L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_PENDIENTE)
                .build()));
        assertThrows(IllegalStateException.class, () -> service.createOrder(request, "lab@hospital.com"));
    }

    @Test
    void validatePayment_acceptsLegacyPendingMuestraWhenPaymentIsPending() {
        LaboratoryPaymentRequest request = new LaboratoryPaymentRequest();
        request.setMetodoPago(PaymentOption.TARJETA);
        request.setDpiPaciente("1234567890123");
        request.setBancoTarjeta("G&T");
        request.setNumeroTarjeta("4111111111111111");
        request.setFechaVencimientoTarjeta("07/28");
        request.setNombreTitularTarjeta("Paciente Demo");
        request.setCvc("123");

        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));

        when(orderRepository.findById(15L)).thenReturn(Optional.of(LaboratoryOrder.builder()
                .ordenLaboratorioId(15L)
                .estado(LaboratoryOrderStatus.PENDIENTE_MUESTRA)
                .pagoValidado(false)
                .citaMedicaDetalleId(11L)
                .nombreExamen("Hemoglobina")
                .build()));

        when(detailsRepository.findById(11L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .pacienteId(77L)
                .build()));

        when(patientRepository.findById(77L)).thenReturn(Optional.of(his.domain.models.Patient.builder()
                .pacienteId(77L)
                .dpi("1234567890123")
                .build()));

        when(orderRepository.save(any(LaboratoryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(resultRepository.findByOrdenId(15L)).thenReturn(Optional.empty());

        var response = service.validatePayment(15L, request, "lab@hospital.com");

        assertEquals(LaboratoryOrderStatus.PENDIENTE_MUESTRA, response.getEstado());
        assertTrue(response.isPagoValidado());
    }
    @Test
    void rejectSample_throwsWhenReasonIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.rejectSample(1L, "   ", "lab@hospital.com"));
    }
    @Test
    void addResult_throwsWhenRangesAreIncomplete() {
        AddLaboratoryResultRequest request = new AddLaboratoryResultRequest();
        request.setOrdenLaboratorioId(5L);
        request.setNombreExamen("Glucosa");
        request.setConclusion("Resultado preliminar");
        request.setValorResultado(new BigDecimal("95"));
        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(LaboratoryOrder.builder()
                .ordenLaboratorioId(5L)
                .estado(LaboratoryOrderStatus.EN_PROCESO)
                .citaMedicaDetalleId(11L)
                .nombreExamen("Glucosa")
                .build()));
        when(detailsRepository.findById(11L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .build()));
        when(resultRepository.findByOrdenId(5L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addResult(request, "lab@hospital.com"));
    }

    @Test
    void rejectSample_throwsWhenOrderAlreadyRejected() {
        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(LaboratoryOrder.builder()
                .ordenLaboratorioId(1L)
                .estado(LaboratoryOrderStatus.MUESTRA_RECHAZADA)
                .build()));

        assertThrows(IllegalStateException.class, () -> service.rejectSample(1L, "Muestra hemolizada", "lab@hospital.com"));
    }

    @Test
    void addResult_throwsWhenResultAlreadyExists() {
        AddLaboratoryResultRequest request = new AddLaboratoryResultRequest();
        request.setOrdenLaboratorioId(5L);
        request.setNombreExamen("Glucosa");
        request.setConclusion("Resultado final");
        request.setValorResultado(new BigDecimal("95"));
        request.setReferenciaMinima(new BigDecimal("70"));
        request.setReferenciaMaxima(new BigDecimal("110"));

        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(LaboratoryOrder.builder()
                .ordenLaboratorioId(5L)
                .estado(LaboratoryOrderStatus.EN_PROCESO)
                .citaMedicaDetalleId(11L)
                .nombreExamen("Glucosa")
                .build()));
        when(detailsRepository.findById(11L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .build()));
        when(resultRepository.findByOrdenId(5L)).thenReturn(Optional.of(LaboratoryResult.builder().resultadoLaboratorioId(10L).build()));

        assertThrows(IllegalStateException.class, () -> service.addResult(request, "lab@hospital.com"));
    }

    @Test
    void addResult_throwsWhenExamNameDoesNotMatchOrder() {
        AddLaboratoryResultRequest request = new AddLaboratoryResultRequest();
        request.setOrdenLaboratorioId(5L);
        request.setNombreExamen("Hemoglobina");
        request.setConclusion("Resultado final");
        request.setValorResultado(new BigDecimal("95"));
        request.setReferenciaMinima(new BigDecimal("70"));
        request.setReferenciaMaxima(new BigDecimal("110"));

        when(userRepository.findByEmail("lab@hospital.com")).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(staffRepository.findByUsuarioId(1L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.LABORATORISTA).personalId(99L).build()));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(LaboratoryOrder.builder()
                .ordenLaboratorioId(5L)
                .estado(LaboratoryOrderStatus.EN_PROCESO)
                .citaMedicaDetalleId(11L)
                .nombreExamen("Glucosa")
                .build()));
        when(detailsRepository.findById(11L)).thenReturn(Optional.of(MedicalAppointmentDetails.builder().citaMedicaId(20L).build()));
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(MedicalAppointment.builder()
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .build()));
        when(resultRepository.findByOrdenId(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.addResult(request, "lab@hospital.com"));
    }
}
