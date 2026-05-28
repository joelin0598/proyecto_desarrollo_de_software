package his.domain.ports;

import his.domain.models.MedicalSpecialityCatalog;

import java.util.List;
import java.util.Optional;

public interface MedicalSpecialityRepository {
    List<MedicalSpecialityCatalog> findAllActive();

    Optional<MedicalSpecialityCatalog> findById(Long especialidadId);
}
