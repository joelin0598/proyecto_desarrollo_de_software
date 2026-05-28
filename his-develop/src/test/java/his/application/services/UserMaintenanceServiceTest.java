package his.application.services;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UpdateHospitalStaffUserRequest;
import his.application.dto.UserMaintenanceResponse;
import his.domain.models.HospitalStaff;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class UserMaintenanceServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private HospitalStaffRepository hospitalStaffRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private UserMaintenanceService userMaintenanceService;
    @BeforeEach
    void setUp() {
        userMaintenanceService = new UserMaintenanceService(
                userRepository,
                hospitalStaffRepository,
                passwordEncoder
        );
    }
    @Test
    void listHospitalStaffUsers_returnsMergedData() {
        User admin = User.builder().userId(9L).email("admin@his.com").role(Role.ADMIN).active(true).build();
        User doctor = User.builder().userId(10L).email("doc@his.com").role(Role.DOCTOR).active(true).build();
        HospitalStaff doctorStaff = HospitalStaff.builder()
                .personalId(200L)
                .usuarioId(10L)
                .nombreCompleto("Dra. Ana Diaz")
                .numeroColegiado("COL-001")
                .build();
        when(userRepository.findAllByRoleNot(Role.PACIENTE)).thenReturn(List.of(admin, doctor));
        when(hospitalStaffRepository.findAll()).thenReturn(List.of(doctorStaff));
        List<UserMaintenanceResponse> result = userMaintenanceService.listHospitalStaffUsers();
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getUserId());
        assertEquals("Dra. Ana Diaz", result.get(0).getNombreCompleto());
        assertEquals("COL-001", result.get(0).getNumeroColegiado());
        assertEquals(9L, result.get(1).getUserId());
    }
    @Test
    void createHospitalStaffUser_rejectsPacienteRole() {
        RegisterRequestAdmin request = RegisterRequestAdmin.builder()
                .nombreCompleto("Persona Paciente")
                .email("paciente@his.com")
                .password("Abc123!")
                .direccion("Zona 1")
                .telefonoCorporativo("55550000")
                .rol(Role.PACIENTE)
                .build();
        assertThrows(IllegalArgumentException.class, () -> userMaintenanceService.createHospitalStaffUser(request));
        verify(userRepository, never()).save(any());
        verify(hospitalStaffRepository, never()).save(any());
    }
    @Test
    void suspendHospitalStaffUser_setsUserInactive() {
        User admin = User.builder().userId(1L).email("admin@his.com").role(Role.ADMIN).active(true).build();
        User target = User.builder().userId(2L).email("enfermeria@his.com").role(Role.ENFERMERA).active(true).build();
        HospitalStaff staff = HospitalStaff.builder().personalId(33L).usuarioId(2L).nombreCompleto("Eva Lopez").build();
        when(userRepository.findByEmail("admin@his.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hospitalStaffRepository.findByUsuarioId(2L)).thenReturn(Optional.of(staff));
        UserMaintenanceResponse result = userMaintenanceService.suspendHospitalStaffUser(2L, "admin@his.com");
        assertFalse(result.isActive());
        assertEquals("Eva Lopez", result.getNombreCompleto());
        verify(userRepository).save(any(User.class));
    }
    @Test
    void suspendHospitalStaffUser_rejectsSelfSuspension() {
        User admin = User.builder().userId(1L).email("admin@his.com").role(Role.ADMIN).active(true).build();
        when(userRepository.findByEmail("admin@his.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> userMaintenanceService.suspendHospitalStaffUser(1L, "admin@his.com")
        );
        assertTrue(error.getMessage().contains("propia cuenta"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateHospitalStaffUser_updatesUserAndStaffData() {
        User target = User.builder().userId(2L).email("doctor@his.com").role(Role.DOCTOR).active(true).build();
        HospitalStaff staff = HospitalStaff.builder()
                .personalId(33L)
                .usuarioId(2L)
                .rol(Role.DOCTOR)
                .nombreCompleto("Dr. Original")
                .numeroColegiado("COL-001")
                .build();
        UpdateHospitalStaffUserRequest request = UpdateHospitalStaffUserRequest.builder()
                .nombreCompleto("Dra. Actualizada")
                .direccion("Zona 11")
                .telefonoCorporativo("55553333")
                .especialidadId(10L)
                .unidadAtencionId(5L)
                .rol(Role.ENFERMERA)
                .numeroColegiado("COL-002")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(hospitalStaffRepository.findByUsuarioId(2L)).thenReturn(Optional.of(staff));
        when(hospitalStaffRepository.existsByNumeroColegiado("COL-002")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hospitalStaffRepository.save(any(HospitalStaff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserMaintenanceResponse result = userMaintenanceService.updateHospitalStaffUser(2L, request);

        assertEquals(Role.ENFERMERA, result.getRole());
        assertEquals("Dra. Actualizada", result.getNombreCompleto());
        assertEquals("COL-002", result.getNumeroColegiado());
        verify(userRepository).save(any(User.class));
        verify(hospitalStaffRepository).save(any(HospitalStaff.class));
    }

    @Test
    void updateHospitalStaffUser_allowsSingleFieldPatch() {
        User target = User.builder().userId(3L).email("administ@his.com").role(Role.ADMINISTRATIVO).active(true).build();
        HospitalStaff staff = HospitalStaff.builder()
                .personalId(44L)
                .usuarioId(3L)
                .rol(Role.ADMINISTRATIVO)
                .nombreCompleto("Perfil Inicial")
                .direccion("Zona 4")
                .telefonoCorporativo("55550000")
                .build();
        UpdateHospitalStaffUserRequest request = UpdateHospitalStaffUserRequest.builder()
                .rol(Role.ADMIN)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(target));
        when(hospitalStaffRepository.findByUsuarioId(3L)).thenReturn(Optional.of(staff));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hospitalStaffRepository.save(any(HospitalStaff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserMaintenanceResponse result = userMaintenanceService.updateHospitalStaffUser(3L, request);

        assertEquals(Role.ADMIN, result.getRole());
        assertEquals("Perfil Inicial", result.getNombreCompleto());
        assertEquals("55550000", result.getTelefonoCorporativo());
    }

    @Test
    void updateHospitalStaffUser_rejectsEmptyPatchBody() {
        User target = User.builder().userId(3L).email("administ@his.com").role(Role.ADMINISTRATIVO).active(true).build();
        HospitalStaff staff = HospitalStaff.builder().personalId(44L).usuarioId(3L).rol(Role.ADMINISTRATIVO).build();
        UpdateHospitalStaffUserRequest request = UpdateHospitalStaffUserRequest.builder().build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(target));
        when(hospitalStaffRepository.findByUsuarioId(3L)).thenReturn(Optional.of(staff));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> userMaintenanceService.updateHospitalStaffUser(3L, request)
        );

        assertTrue(error.getMessage().contains("al menos un campo"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteHospitalStaffUser_deletesStaffAndUser() {
        User admin = User.builder().userId(1L).email("admin@his.com").role(Role.ADMIN).active(true).build();
        User target = User.builder().userId(2L).email("recepcion@his.com").role(Role.RECEPCION).active(true).build();

        when(userRepository.findByEmail("admin@his.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        userMaintenanceService.deleteHospitalStaffUser(2L, "admin@his.com");

        verify(hospitalStaffRepository).deleteByUsuarioId(2L);
        verify(userRepository).deleteById(2L);
        verify(userRepository, never()).deleteById(1L);
        verify(hospitalStaffRepository, never()).deleteByUsuarioId(1L);
    }
}
