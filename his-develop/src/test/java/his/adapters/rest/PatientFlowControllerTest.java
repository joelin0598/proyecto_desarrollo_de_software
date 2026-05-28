package his.adapters.rest;

import his.application.dto.PatientRegisterRequest;
import his.application.dto.PatientRegisterResponse;
import his.application.dto.PatientTriageRequest;
import his.application.dto.PatientAvailabilityResponse;
import his.application.dto.TriageResponse;
import his.application.services.PatientFlowService;
import his.domain.models.PatientGender;
import his.domain.models.Priority;
import his.domain.models.PaymentOption;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientFlowControllerTest {

    @Mock
    private PatientFlowService service;

    @InjectMocks
    private PatientFlowController controller;

    @Test
    void checkAvailability_returnsOk() {
        when(service.checkAvailability("1234567890123", "ana@email.com"))
                .thenReturn(PatientAvailabilityResponse.builder()
                        .dpiInUse(false)
                        .emailInUse(false)
                        .available(true)
                        .message("Disponibilidad valida para continuar con el registro.")
                        .build());

        ResponseEntity<PatientAvailabilityResponse> response = controller.checkAvailability("1234567890123", "ana@email.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isAvailable());
    }

    @Test
    void register_returnsCreated_whenHappyPath() {
        PatientRegisterRequest request = new PatientRegisterRequest();
        request.setNombreCompleto("Ana Torres");
        request.setDpi("1234567890123");
        request.setGenero(PatientGender.FEMENINO);
        request.setContactoEmergencia("Pedro Torres");
        request.setTelefonoEmergencia("55551234");
        request.setMetodoPago(PaymentOption.TARJETA);

        when(service.register(any(PatientRegisterRequest.class), eq("recepcion@hospital.com")))
                .thenReturn(PatientRegisterResponse.builder()
                        .pacienteId(10L)
                        .citaMedicaId(20L)
                        .pacienteNuevo(true)
                        .pagoValidado(true)
                        .mensaje("Registro completado y pago validado")
                        .build());

        UserDetails user = new User("recepcion@hospital.com", "x", Collections.emptyList());
        ResponseEntity<PatientRegisterResponse> response = controller.register(request, user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getPacienteId());
        assertTrue(response.getBody().isPagoValidado());
        verify(service).register(any(PatientRegisterRequest.class), eq("recepcion@hospital.com"));
    }

    @Test
    void register_returnsBadRequest_whenFA04SaldoInsuficiente() {
        IllegalArgumentException ex = new IllegalArgumentException("FA04: saldo insuficiente en tarjeta");
        ResponseEntity<?> response = controller.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("FA04"));
    }

    @Test
    void register_returnsBadRequest_whenFA05SinMetodoPago() {
        IllegalArgumentException ex = new IllegalArgumentException("FA05: Debe registrar un metodo de pago para continuar.");
        ResponseEntity<?> response = controller.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("FA05"));
    }

    @Test
    void triage_returnsCreated_whenHappyPath() {
        PatientTriageRequest request = new PatientTriageRequest();
        request.setCitaMedicaId(20L);
        request.setPresionSistolica(120);
        request.setPresionDiastolica(80);
        request.setFrecuenciaCardiaca(72);
        request.setTemperatura(36.8);
        request.setSaturacionOxigeno(98);
        request.setPesoKg(60);
        request.setTallaCm(165);

        when(service.triage(any(PatientTriageRequest.class), eq("recepcion@hospital.com")))
                .thenReturn(TriageResponse.builder()
                        .pacienteId(10L)
                        .citaMedicaId(20L)
                        .prioridad(Priority.VERDE)
                        .alertaEmergencia(false)
                        .build());

        UserDetails user = new User("recepcion@hospital.com", "x", Collections.emptyList());
        ResponseEntity<TriageResponse> response = controller.triage(request, user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Priority.VERDE, response.getBody().getPrioridad());
        verify(service).triage(any(PatientTriageRequest.class), eq("recepcion@hospital.com"));
    }
}


