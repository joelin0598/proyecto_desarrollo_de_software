package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalPrescriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicalPrescriptionJpaRepository extends JpaRepository<MedicalPrescriptionJpaEntity, Long> {
    Optional<MedicalPrescriptionJpaEntity> findTopByCitaMedicaDetalleCitaMedicaDetalleIdOrderByCreatedAtDesc(Long citaMedicaDetalleId);

    @Query("""
            SELECT receta
            FROM MedicalPrescriptionJpaEntity receta
            JOIN receta.citaMedicaDetalle detalle
            JOIN detalle.citaMedica cita
            JOIN cita.paciente paciente
            WHERE paciente.dpi = :dpi
            ORDER BY receta.createdAt DESC
            """)
    List<MedicalPrescriptionJpaEntity> findByPacienteDpiOrderByCreatedAtDesc(String dpi);
}

