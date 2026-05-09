package his.domain.ports;

import his.domain.models.InsuranceCatalog;

import java.util.List;

public interface InsuranceCatalogRepository {
    List<InsuranceCatalog> findAllActive();
}

