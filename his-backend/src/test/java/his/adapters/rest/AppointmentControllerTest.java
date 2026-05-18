package his.adapters.rest;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.usecases.AppointmentUseCase;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PaymentOption;
import his.domain.models.StatusAppointment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentUseCase appointmentUseCase;

    @InjectMocks
    private AppointmentController appointmentController;

    @Test
    void scheduleAppointment_returnsCreated() {
        ScheduleAppointmentRequest request = ScheduleAppointmentRequest.builder()
                .pacienteId(1L)
                .medicoPersonalId(2L)
                .fechaCita(LocalDate.now().plusDays(2))
                .horaCita(LocalTime.of(8, 0))
                .motivoConsulta("Control")
                .metodoPago(PaymentOption.TARJETA)
                .bancoTarjeta("Banco")
                .numeroTarjeta("4111111111111111")
                .fechaVencimientoTarjeta("12/30")
                .nombreTitularTarjeta("TEST")
                .cvc("123")
                .build();

        ScheduleAppointmentResponse expected = ScheduleAppointmentResponse.builder()
                .citaMedicaId(10L)
                .pacienteId(1L)
                .medicoPersonalId(2L)
                .estadoCita(StatusAppointment.PROGRAMADA)
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .pagoValidado(true)
                .build();

        UserDetails userDetails = new User("recepcion@hospital.com", "pass", Collections.emptyList());
        when(appointmentUseCase.scheduleAppointment(any(ScheduleAppointmentRequest.class), eq("recepcion@hospital.com")))
                .thenReturn(expected);

        ResponseEntity<ScheduleAppointmentResponse> response = appointmentController.scheduleAppointment(request, userDetails);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getCitaMedicaId());
        verify(appointmentUseCase).scheduleAppointment(any(ScheduleAppointmentRequest.class), eq("recepcion@hospital.com"));
    }

    @Test
    void listAppointments_returnsOk() {
        when(appointmentUseCase.listAppointments()).thenReturn(List.of(
                ScheduleAppointmentResponse.builder().citaMedicaId(10L).build()
        ));

        ResponseEntity<List<ScheduleAppointmentResponse>> response = appointmentController.listAppointments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(appointmentUseCase).listAppointments();
    }
}