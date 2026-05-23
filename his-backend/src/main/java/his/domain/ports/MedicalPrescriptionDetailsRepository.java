package his.domain.ports;

import his.domain.models.MedicalPrescriptionDetails;

import java.util.List;
import java.util.Optional;

public interface MedicalPrescriptionDetailsRepository {
    MedicalPrescriptionDetails save(MedicalPrescriptionDetails details);
    Optional<MedicalPrescriptionDetails> findById(Long id);
    List<MedicalPrescriptionDetails> findByRecetaId(Long recetaMedicaId);
}

