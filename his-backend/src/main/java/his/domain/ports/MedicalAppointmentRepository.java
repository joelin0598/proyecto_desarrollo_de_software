package his.domain.ports;

import his.domain.models.MedicalAppointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MedicalAppointmentRepository {
    MedicalAppointment save(MedicalAppointment appointment);

    boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita);

    List<MedicalAppointment> findAllOrderByDateTimeDesc();
}
