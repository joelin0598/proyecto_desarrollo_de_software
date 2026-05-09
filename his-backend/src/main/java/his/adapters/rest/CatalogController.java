package his.adapters.rest;

import his.application.dto.InsuranceOptionResponse;
import his.application.dto.PatientGenderOptionResponse;
import his.application.usecases.CatalogUseCase;
import his.domain.models.PatientGender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogUseCase catalogUseCase;

    @GetMapping("/patient-genders")
    public ResponseEntity<List<PatientGenderOptionResponse>> patientGenders() {
        List<PatientGenderOptionResponse> options = catalogUseCase.getPatientGenders().stream()
                .map(this::toGenderOption)
                .toList();
        return ResponseEntity.ok(options);
    }

    @GetMapping("/insurances")
    public ResponseEntity<List<InsuranceOptionResponse>> insurances() {
        List<InsuranceOptionResponse> options = catalogUseCase.getActiveInsurances().stream()
                .map(insurance -> InsuranceOptionResponse.builder()
                        .id(insurance.getAseguradoraId())
                        .nombre(insurance.getNombre())
                        .build())
                .toList();
        return ResponseEntity.ok(options);
    }

    private PatientGenderOptionResponse toGenderOption(PatientGender gender) {
        return PatientGenderOptionResponse.builder()
                .code(gender.name())
                .label(toLabel(gender))
                .build();
    }

    private String toLabel(PatientGender gender) {
        return switch (gender) {
            case MASCULINO -> "Masculino";
            case FEMENINO -> "Femenino";
            case NO_ESPECIFICA -> "No especifica";
        };
    }
}

