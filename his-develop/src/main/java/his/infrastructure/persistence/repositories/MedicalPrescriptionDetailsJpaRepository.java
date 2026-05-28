package his.infrastructure.persistence.repositories;

import his.infrastructure.persistence.entities.MedicalPrescriptionDetailsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalPrescriptionDetailsJpaRepository extends JpaRepository<MedicalPrescriptionDetailsJpaEntity, Long> {
    List<MedicalPrescriptionDetailsJpaEntity> findByRecetaMedicaRecetaMedicaIdOrderByCreatedAtAsc(Long recetaMedicaId);
}

