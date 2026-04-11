package his.domain.ports;

import his.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient_PatientIdOrderByAppointmentDateAsc(Long patientId);
}
