package his.application.usecases;

import his.application.dto.TriageRequest;
import his.domain.models.VitalSigns;

public interface TriageUseCase {
    VitalSigns execute(TriageRequest request);

}
