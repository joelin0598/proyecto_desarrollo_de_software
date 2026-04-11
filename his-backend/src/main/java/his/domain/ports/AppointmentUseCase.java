package his.domain.ports;

import his.application.dto.AppointmentRequest;
import his.application.dto.AppointmentResponse;

import java.util.List;

/**
 * Puerto de aplicación para la gestión de citas (CU-0).
 */
public interface AppointmentUseCase {

    AppointmentResponse scheduleAppointment(AppointmentRequest request);

    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);

    AppointmentResponse cancelAppointment(Long appointmentId);
}
