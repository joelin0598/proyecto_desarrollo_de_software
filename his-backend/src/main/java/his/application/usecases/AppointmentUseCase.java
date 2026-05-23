package his.application.usecases;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;

import java.util.List;

public interface AppointmentUseCase {
    ScheduleAppointmentResponse scheduleAppointment(ScheduleAppointmentRequest request, String emailSolicitante);

    List<ScheduleAppointmentResponse> listAppointments();

    default List<ScheduleAppointmentResponse> listAppointments(String emailSolicitante) {
        return listAppointments();
    }
}
