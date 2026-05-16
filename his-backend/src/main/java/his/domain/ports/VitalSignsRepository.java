package his.domain.ports;


import his.application.dto.TriageListItemsResponse;
import his.domain.models.VitalSigns;

import java.util.List;

public interface VitalSignsRepository {
    VitalSigns save(VitalSigns vitalSigns);
    List<VitalSigns> findAllRecent();
}