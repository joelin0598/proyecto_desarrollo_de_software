package his.application.services;
import his.application.dto.DispenseMedicineRequest;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.MedicalPrescription;
import his.domain.models.MedicalPrescriptionDetails;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalPrescriptionDetailsRepository;
import his.domain.ports.MedicalPrescriptionRepository;
import his.domain.ports.MedicationReminderRepository;
import his.domain.ports.MedicineRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class PharmacyServiceTest {
    @Mock
    private MedicalPrescriptionRepository prescriptionRepository;
    @Mock
    private MedicalPrescriptionDetailsRepository prescriptionDetailsRepository;
    @Mock
    private MedicineRepository medicineRepository;
    @Mock
    private MedicationReminderRepository reminderRepository;
    @Mock
    private MedicalAppointmentDetailsRepository appointmentDetailsRepository;
    @Mock
    private MedicalAppointmentRepository appointmentRepository;
    @Mock
    private HospitalStaffRepository staffRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    private PharmacyService service;
    @BeforeEach
    void setUp() {
        service = new PharmacyService(
                prescriptionRepository,
                prescriptionDetailsRepository,
                medicineRepository,
                reminderRepository,
                appointmentDetailsRepository,
                appointmentRepository,
                staffRepository,
                patientRepository,
                userRepository
        );
    }
    @Test
    void getPrescription_throwsWhenPrescriptionExpired() {
        when(prescriptionRepository.findByCitaMedicaDetalleId(77L)).thenReturn(Optional.of(
                MedicalPrescription.builder()
                        .recetaMedicaId(1L)
                        .citaMedicaDetalleId(77L)
                        .fechaEmision(LocalDate.now().minusDays(40))
                        .build()
        ));
        assertThrows(IllegalStateException.class, () -> service.getPrescription(77L));
    }
    @Test
    void dispense_throwsWhenAdministrativeStatusIsPending() {
        DispenseMedicineRequest request = new DispenseMedicineRequest();
        request.setRecetaMedicaDetalleId(500L);
        when(userRepository.findByEmail("farmacia@hospital.com")).thenReturn(Optional.of(User.builder().userId(3L).build()));
        when(staffRepository.findByUsuarioId(3L)).thenReturn(Optional.of(HospitalStaff.builder().rol(Role.FARMACEUTICO).personalId(12L).build()));
        when(prescriptionDetailsRepository.findById(500L)).thenReturn(Optional.of(
                MedicalPrescriptionDetails.builder()
                        .recetaMedicaDetalleId(500L)
                        .recetaMedicaId(900L)
                        .despachado(false)
                        .cantidad(1)
                        .medicamentoId(700L)
                        .build()
        ));
        when(prescriptionRepository.findById(900L)).thenReturn(Optional.of(
                MedicalPrescription.builder()
                        .recetaMedicaId(900L)
                        .citaMedicaDetalleId(66L)
                        .fechaEmision(LocalDate.now())
                        .build()
        ));
        when(appointmentDetailsRepository.findById(66L)).thenReturn(Optional.of(
                MedicalAppointmentDetails.builder().citaMedicaId(44L).build()
        ));
        when(appointmentRepository.findById(44L)).thenReturn(Optional.of(
                MedicalAppointment.builder().estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_PENDIENTE).build()
        ));
        assertThrows(IllegalStateException.class, () -> service.dispense(request, "farmacia@hospital.com"));
    }
}
