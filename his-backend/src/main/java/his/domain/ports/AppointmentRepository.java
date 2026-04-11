package his.domain.ports;

import his.domain.AppointmentEntity;
import his.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByPatient_UserId(Long patientId);
    List<AppointmentEntity> findByStatus(AppointmentStatus status);
}
