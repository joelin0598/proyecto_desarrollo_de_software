package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalAppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MedicalAppointmentJpaRepository extends JpaRepository<MedicalAppointmentJpaEntity, Long> {
    boolean existsByPersonalPersonalIdAndFechaCitaAndHoraCitaAndIsActiveTrue(
            Long personalId,
            LocalDate fechaCita,
            LocalTime horaCita
    );

    List<MedicalAppointmentJpaEntity> findAllByOrderByFechaCitaDescHoraCitaDesc();
}
