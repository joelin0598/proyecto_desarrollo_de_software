package his.domain.ports;

import his.domain.models.Medicine;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository {
    Optional<Medicine> findById(Long id);
    List<Medicine> findAllActive();
    Medicine save(Medicine medicine);
}

