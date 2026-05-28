package his.domain.ports;

import his.domain.models.LaboratoryOrder;

import java.util.List;
import java.util.Optional;

public interface LaboratoryOrderRepository {
    LaboratoryOrder save(LaboratoryOrder order);
    Optional<LaboratoryOrder> findById(Long id);
    List<LaboratoryOrder> findByCitaMedicaDetalleId(Long citaMedicaDetalleId);
}

