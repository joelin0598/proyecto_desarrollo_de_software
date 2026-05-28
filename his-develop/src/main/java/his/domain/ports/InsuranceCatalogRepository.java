package his.domain.ports;

import his.domain.models.InsuranceCatalog;

import java.util.List;
import java.util.Optional;

public interface InsuranceCatalogRepository {
    List<InsuranceCatalog> findAllActive();

    Optional<InsuranceCatalog> findById(Long aseguradoraId);
}