package his.domain.ports;

import his.domain.models.MedicalPrescription;

import java.util.Optional;

public interface MedicalPrescriptionRepository {
    MedicalPrescription save(MedicalPrescription prescription);
    Optional<MedicalPrescription> findById(Long id);
    Optional<MedicalPrescription> findByCitaMedicaDetalleId(Long citaMedicaDetalleId);
}

