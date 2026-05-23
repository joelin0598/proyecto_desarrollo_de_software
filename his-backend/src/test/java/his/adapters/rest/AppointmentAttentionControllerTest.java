package his.adapters.rest;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;
import his.application.usecases.MedicalAppointmentAttentionUseCase;
import his.domain.models.StatusAppointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CU06 — Tests unitarios del controlador REST de atención médica.
 * Cubre: GET /queue, GET /current, POST /open, PATCH /{id}/close.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentAttentionControllerTest {

    @Mock
    private MedicalAppointmentAttentionUseCase useCase;

    private AppointmentAttentionController controller;

    private static final String EMAIL_DOCTOR = "doctor@hospital.com";
    private static final Long CITA_ID = 100L;
    private static final Long DETALLE_ID = 200L;

    @BeforeEach
    void setUp() {
        controller = new AppointmentAttentionController(useCase);
        // Simular usuario autenticado en el SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(EMAIL_DOCTOR, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/appointments/attention/queue
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getQueue_returnsOk_withQueueList() {
        // Arrange
        MedicalAppointmentQueueItemResponse item = MedicalAppointmentQueueItemResponse.builder()
                .citaMedicaId(CITA_ID)
                .pacienteNombre("Ana Torres")
                .prioridad("VERDE")
                .build();
        when(useCase.getPatientQueue(EMAIL_DOCTOR)).thenReturn(List.of(item));

        // Act
        ResponseEntity<List<MedicalAppointmentQueueItemResponse>> response = controller.getQueue();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(CITA_ID, response.getBody().get(0).getCitaMedicaId());
        verify(useCase).getPatientQueue(EMAIL_DOCTOR);
    }

    @Test
    void getQueue_returnsOk_withEmptyList_FA02() {
        // Arrange — FA02: cola vacía
        when(useCase.getPatientQueue(EMAIL_DOCTOR)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<MedicalAppointmentQueueItemResponse>> response = controller.getQueue();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty(), "FA02: cola vacía retorna 200 con lista vacía");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/appointments/attention/current
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrent_returnsOk_whenAttentionExists() {
        // Arrange
        MedicalAppointmentAttentionResponse attencion = MedicalAppointmentAttentionResponse.builder()
                .citaMedicaId(CITA_ID)
                .estado(StatusAppointment.EN_CURSO)
                .pacienteNombre("Ana Torres")
                .build();
        when(useCase.getCurrentAttention(EMAIL_DOCTOR)).thenReturn(attencion);

        // Act
        ResponseEntity<MedicalAppointmentAttentionResponse> response = controller.getCurrent();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusAppointment.EN_CURSO, response.getBody().getEstado());
    }

    @Test
    void getCurrent_returnsNoContent_whenNoOpenAttention() {
        // Arrange — el médico no tiene cita en curso
        when(useCase.getCurrentAttention(EMAIL_DOCTOR)).thenReturn(null);

        // Act
        ResponseEntity<MedicalAppointmentAttentionResponse> response = controller.getCurrent();

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/appointments/attention/open
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void open_returnsOk_withAttentionResponse() {
        // Arrange
        MedicalAppointmentAttentionResponse attencion = MedicalAppointmentAttentionResponse.builder()
                .citaMedicaId(CITA_ID)
                .citaMedicaDetalleId(DETALLE_ID)
                .estado(StatusAppointment.EN_CURSO)
                .pacienteNombre("Ana Torres")
                .build();
        when(useCase.openAttention(eq(CITA_ID), eq(EMAIL_DOCTOR))).thenReturn(attencion);

        // Act
        ResponseEntity<MedicalAppointmentAttentionResponse> response = controller.open(CITA_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusAppointment.EN_CURSO, response.getBody().getEstado());
        assertEquals(DETALLE_ID, response.getBody().getCitaMedicaDetalleId());
        verify(useCase).openAttention(CITA_ID, EMAIL_DOCTOR);
    }

    @Test
    void open_throwsIllegalState_whenDoctorAlreadyHasOpenAttention_FA01() {
        // FA01: el handler de excepciones debe retornar 400 al recibir IllegalStateException
        // El controller delega al handler cuando el use case lanza. Probamos el handler directamente.
        ResponseEntity<?> response = controller.handleValidation(
                new IllegalStateException("El medico ya tiene una cita en curso."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/appointments/attention/{detalleId}/close
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void close_returnsOk_withAttendedResponse_RN13() {
        // Arrange
        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("Paciente estable")
                .diagnostico("Hipertensión leve")
                .recetaMedica("Losartán 50mg")
                .requiereSeguimiento(false)
                .build();

        MedicalAppointmentAttentionResponse attencion = MedicalAppointmentAttentionResponse.builder()
                .citaMedicaId(CITA_ID)
                .citaMedicaDetalleId(DETALLE_ID)
                .estado(StatusAppointment.ATENDIDA)
                .diagnostico("Hipertensión leve")
                .requiereSeguimiento(false)
                .build();
        when(useCase.closeAttention(any(CloseMedicalAppointmentAttentionRequest.class), eq(EMAIL_DOCTOR)))
                .thenReturn(attencion);

        // Act
        ResponseEntity<MedicalAppointmentAttentionResponse> response = controller.close(DETALLE_ID, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatusAppointment.ATENDIDA, response.getBody().getEstado());
        assertEquals("Hipertensión leve", response.getBody().getDiagnostico());
        verify(useCase).closeAttention(any(CloseMedicalAppointmentAttentionRequest.class), eq(EMAIL_DOCTOR));
    }

    @Test
    void close_throwsIllegalState_whenCitaNotEnCurso_FA03() {
        // FA03: el handler de excepciones debe retornar 400 cuando la cita no está EN_CURSO.
        // Probamos el handler directamente para verificar el comportamiento del controlador.
        ResponseEntity<?> response = controller.handleValidation(
                new IllegalStateException("La cita no esta en curso o ya fue finalizada."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Exception handlers
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void handleValidation_returnsBadRequest_forIllegalArgument() {
        // Act
        ResponseEntity<?> response = controller.handleValidation(
                new IllegalArgumentException("Cita no encontrada"));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleGeneral_returnsInternalServerError() {
        // Act
        ResponseEntity<?> response = controller.handleGeneral(new RuntimeException("Error inesperado"));

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}



