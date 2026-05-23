package his.domain.ports;

import his.domain.models.MedicalAppointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MedicalAppointmentRepository {
    MedicalAppointment save(MedicalAppointment appointment);

    boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita);

    List<MedicalAppointment> findAllOrderByDateTimeDesc();

    List<MedicalAppointment> findByPacienteIdOrderByDateTimeDesc(Long pacienteId);

    Optional<MedicalAppointment> findById(Long citaMedicaId);

    Optional<MedicalAppointment> findOpenByDoctor(Long personalId);

    /**
     * CU06 / RN09: cola de espera — citas PROGRAMADAS con PAGO_VALIDADO,
     * ordenadas por fecha+hora asc.
     */
    List<MedicalAppointment> findPendingQueueByDoctor(Long personalId);

    Optional<MedicalAppointment> findNextPaidScheduledByPatient(Long pacienteId);
}
