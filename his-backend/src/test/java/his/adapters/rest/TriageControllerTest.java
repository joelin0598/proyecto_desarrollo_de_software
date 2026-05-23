package his.adapters.rest;

import his.application.dto.TriageListItemsResponse;
import his.application.dto.TriagePaidAppointmentLookupResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.application.usecases.TriageUseCase;
import his.domain.models.PatientGender;
import his.domain.models.Priority;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageControllerTest {

    @Mock
    private TriageUseCase triageUseCase;

    @InjectMocks
    private TriageController triageController;

    @Test
    void registrarTriaje_returnsCreatedResponse_whenRequestIsValid() {
        // Arrange
        TriageRequest request = TriageRequest.builder()
                .nombreCompleto("Ana García")
                .dpi("1234567890123")
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

        TriageResponse expectedResponse = TriageResponse.builder()
                .pacienteId(5L)
                .nombreCompleto("Ana García")
                .dpi("1234567890123")
                .pacienteNuevo(true)
                .signosVitalesId(99L)
                .prioridad(Priority.VERDE)
                .alertaEmergencia(false)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(72)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .pesoKg(60)
                .tallaCm(165)
                .build();

        UserDetails userDetails = new User("enfermera@hospital.com", "pass", Collections.emptyList());
        when(triageUseCase.execute(any(TriageRequest.class), eq("enfermera@hospital.com")))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<TriageResponse> response = triageController.registrarTriaje(request, userDetails);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getPacienteId());
        assertEquals(Priority.VERDE, response.getBody().getPrioridad());
        assertFalse(response.getBody().isAlertaEmergencia());
        verify(triageUseCase).execute(any(TriageRequest.class), eq("enfermera@hospital.com"));
    }

    @Test
    void listarTriajesRecientes_returnsOk() {
        TriageListItemsResponse item = TriageListItemsResponse.builder()
                .signosVitalesId(99L)
                .pacienteId(5L)
                .nombreCompleto("Ana Garcia")
                .dpi("1234567890123")
                .prioridad(Priority.VERDE)
                .alertaEmergencia(false)
                .build();

        when(triageUseCase.listarTriajesRecientes()).thenReturn(List.of(item));

        ResponseEntity<List<TriageListItemsResponse>> response = triageController.listarTriajesRecientes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(99L, response.getBody().get(0).getSignosVitalesId());
        verify(triageUseCase).listarTriajesRecientes();
    }

    @Test
    void findPaidAppointmentByDpi_returnsOk_whenFound() {
        TriagePaidAppointmentLookupResponse dto = TriagePaidAppointmentLookupResponse.builder()
                .citaMedicaId(10L)
                .pacienteId(5L)
                .pacienteDpi("1234567890123")
                .estadoAdministrativo("PAGO_VALIDADO")
                .build();

        when(triageUseCase.findPaidAppointmentByDpi("1234567890123")).thenReturn(Optional.of(dto));

        ResponseEntity<TriagePaidAppointmentLookupResponse> response =
                triageController.findPaidAppointmentByDpi("1234567890123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getCitaMedicaId());
        verify(triageUseCase).findPaidAppointmentByDpi("1234567890123");
    }

    @Test
    void findPaidAppointmentByDpi_returnsNotFound_whenMissing() {
        when(triageUseCase.findPaidAppointmentByDpi("1234567890123")).thenReturn(Optional.empty());

        ResponseEntity<TriagePaidAppointmentLookupResponse> response =
                triageController.findPaidAppointmentByDpi("1234567890123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(triageUseCase).findPaidAppointmentByDpi("1234567890123");
    }
}
