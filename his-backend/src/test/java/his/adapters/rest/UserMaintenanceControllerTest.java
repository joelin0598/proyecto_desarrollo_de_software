package his.adapters.rest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UpdateHospitalStaffUserRequest;
import his.application.dto.UserMaintenanceResponse;
import his.application.usecases.UserMaintenanceUseCase;
import his.domain.models.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class UserMaintenanceControllerTest {
    @Mock
    private UserMaintenanceUseCase userMaintenanceUseCase;
    @InjectMocks
    private UserMaintenanceController userMaintenanceController;
    @Test
    void listHospitalStaffUsers_returnsOk() {
        UserMaintenanceResponse item = UserMaintenanceResponse.builder()
                .userId(25L)
                .email("doctor@his.com")
                .role(Role.DOCTOR)
                .active(true)
                .nombreCompleto("Dr. Mario Gomez")
                .build();
        when(userMaintenanceUseCase.listHospitalStaffUsers()).thenReturn(List.of(item));
        ResponseEntity<List<UserMaintenanceResponse>> response = userMaintenanceController.listHospitalStaffUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("doctor@his.com", response.getBody().get(0).getEmail());
        verify(userMaintenanceUseCase).listHospitalStaffUsers();
    }
    @Test
    void createHospitalStaffUser_returnsCreated() {
        RegisterRequestAdmin request = RegisterRequestAdmin.builder()
                .nombreCompleto("Dra. Laura Rivas")
                .email("laura.rivas@his.com")
                .password("Abc123!")
                .direccion("Zona 10")
                .telefonoCorporativo("55554444")
                .rol(Role.DOCTOR)
                .numeroColegiado("COL-999")
                .build();
        UserMaintenanceResponse responseBody = UserMaintenanceResponse.builder()
                .userId(50L)
                .email("laura.rivas@his.com")
                .role(Role.DOCTOR)
                .active(true)
                .build();
        when(userMaintenanceUseCase.createHospitalStaffUser(request)).thenReturn(responseBody);
        ResponseEntity<UserMaintenanceResponse> response = userMaintenanceController.createHospitalStaffUser(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(50L, response.getBody().getUserId());
        verify(userMaintenanceUseCase).createHospitalStaffUser(request);
    }
    @Test
    void suspendHospitalStaffUser_returnsOk() {
        UserDetails adminDetails = new User("admin@his.com", "pass", Collections.emptyList());
        UserMaintenanceResponse responseBody = UserMaintenanceResponse.builder()
                .userId(90L)
                .email("enfermeria@his.com")
                .role(Role.ENFERMERA)
                .active(false)
                .build();
        when(userMaintenanceUseCase.suspendHospitalStaffUser(90L, "admin@his.com")).thenReturn(responseBody);
        ResponseEntity<UserMaintenanceResponse> response = userMaintenanceController.suspendHospitalStaffUser(90L, adminDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().isActive());
        verify(userMaintenanceUseCase).suspendHospitalStaffUser(90L, "admin@his.com");
    }

    @Test
    void updateHospitalStaffUser_returnsOk() {
        UpdateHospitalStaffUserRequest request = UpdateHospitalStaffUserRequest.builder()
                .rol(Role.DOCTOR)
                .build();
        UserMaintenanceResponse responseBody = UserMaintenanceResponse.builder()
                .userId(44L)
                .email("doctor@his.com")
                .role(Role.DOCTOR)
                .active(true)
                .nombreCompleto("Perfil sin cambios")
                .build();

        when(userMaintenanceUseCase.updateHospitalStaffUser(44L, request)).thenReturn(responseBody);

        ResponseEntity<UserMaintenanceResponse> response = userMaintenanceController.updateHospitalStaffUser(44L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Perfil sin cambios", response.getBody().getNombreCompleto());
        verify(userMaintenanceUseCase).updateHospitalStaffUser(44L, request);
    }

    @Test
    void deleteHospitalStaffUser_returnsNoContent() {
        UserDetails adminDetails = new User("admin@his.com", "pass", Collections.emptyList());

        ResponseEntity<Void> response = userMaintenanceController.deleteHospitalStaffUser(88L, adminDetails);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userMaintenanceUseCase).deleteHospitalStaffUser(88L, "admin@his.com");
    }
}
