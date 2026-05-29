package his.application.services;
import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.LaboratoryOrder;
import his.domain.models.LaboratoryOrderStatus;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.LaboratoryOrderRepository;
import his.domain.ports.LaboratoryResultRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                userRepository
        );
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
                .build()));
        assertThrows(IllegalArgumentException.class, () -> service.addResult(request, "lab@hospital.com"));
    }
}
