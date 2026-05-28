package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalAppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MedicalAppointmentJpaRepository extends JpaRepository<MedicalAppointmentJpaEntity, Long> {
    boolean existsByPersonalPersonalIdAndFechaCitaAndHoraCitaAndIsActiveTrue(
            Long personalId,
            LocalDate fechaCita,
            LocalTime horaCita
    );

    List<MedicalAppointmentJpaEntity> findAllByOrderByFechaCitaDescHoraCitaDesc();

    List<MedicalAppointmentJpaEntity> findByPacientePacienteIdOrderByFechaCitaDescHoraCitaDesc(Long pacienteId);

    List<MedicalAppointmentJpaEntity> findByPersonalPersonalIdAndEstadoCitaAndEstadoAdministrativoAndIsActiveTrueOrderByFechaCitaAscHoraCitaAsc(
            Long personalId,
            his.domain.models.StatusAppointment estadoCita,
            his.domain.models.AdministrativeAppointmentStatus estadoAdministrativo);

    @Query("""
            SELECT cm
            FROM MedicalAppointmentJpaEntity cm
            WHERE cm.isActive = true
              AND cm.estadoCita = his.domain.models.StatusAppointment.PROGRAMADA
              AND cm.estadoAdministrativo = his.domain.models.AdministrativeAppointmentStatus.PAGO_VALIDADO
              AND cm.solvenciaPago = true
              AND (
                   cm.citaProgramada = false
                   OR cm.personal IS NULL
                   OR cm.personal.personalId = :personalId
              )
            ORDER BY
              CASE cm.prioridad
                WHEN his.domain.models.Priority.ROJO THEN 1
                WHEN his.domain.models.Priority.NARANJA THEN 2
                WHEN his.domain.models.Priority.AMARILLO THEN 3
                WHEN his.domain.models.Priority.VERDE THEN 4
                ELSE 5
              END,
              COALESCE(cm.fechaHoraTriaje, cm.createdAt) ASC,
              cm.fechaCita ASC,
              cm.horaCita ASC,
              cm.createdAt ASC
            """)
    List<MedicalAppointmentJpaEntity> findPendingAttentionQueue(Long personalId);

    Optional<MedicalAppointmentJpaEntity> findTopByPersonalPersonalIdAndEstadoCitaAndIsActiveTrueOrderByUpdatedAtDesc(
            Long personalId,
            his.domain.models.StatusAppointment estadoCita);

    Optional<MedicalAppointmentJpaEntity> findTopByPacientePacienteIdAndEstadoCitaAndEstadoAdministrativoAndIsActiveTrueOrderByFechaCitaAscHoraCitaAsc(
            Long pacienteId,
            his.domain.models.StatusAppointment estadoCita,
            his.domain.models.AdministrativeAppointmentStatus estadoAdministrativo);
}
