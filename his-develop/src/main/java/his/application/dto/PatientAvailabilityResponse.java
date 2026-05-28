package his.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientAvailabilityResponse {
    private boolean dpiInUse;
    private boolean emailInUse;
    private boolean available;
    private String message;
}

