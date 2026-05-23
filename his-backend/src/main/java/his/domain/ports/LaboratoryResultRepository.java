package his.domain.ports;

import his.domain.models.LaboratoryResult;

import java.util.Optional;

public interface LaboratoryResultRepository {
    LaboratoryResult save(LaboratoryResult result);
    Optional<LaboratoryResult> findByOrdenId(Long ordenLaboratorioId);
}

