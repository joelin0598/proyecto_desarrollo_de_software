package his.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PharmacyPrescriptionLookupResponse {
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private List<PrescriptionResponse> recetas;
}
