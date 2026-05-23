package his.adapters.rest;

import his.application.dto.DoctorOptionResponse;
import his.application.dto.CareUnitOptionResponse;
import his.application.dto.InsuranceOptionResponse;
import his.application.dto.PatientGenderOptionResponse;
import his.application.dto.SpecialtyOptionResponse;
import his.application.usecases.CatalogUseCase;
import his.domain.models.CareUnit;
import his.domain.models.PatientGender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtyOptionResponse>> specialties() {
        List<SpecialtyOptionResponse> options = catalogUseCase.getActiveSpecialties().stream()
                .map(s -> SpecialtyOptionResponse.builder()
                        .id(s.getEspecialidadMedicaId())
                        .nombre(s.getNombre())
                        .descripcion(s.getDescripcion())
                        .build())
                .toList();
        return ResponseEntity.ok(options);
    }

    @GetMapping("/care-units")
    public ResponseEntity<List<CareUnitOptionResponse>> careUnits() {
        List<CareUnitOptionResponse> options = catalogUseCase.getCareUnits().stream()
                .map(unit -> CareUnitOptionResponse.builder()
                        .id((long) unit.ordinal() + 1)
                        .nombre(toLabel(unit))
                        .build())
                .toList();
        return ResponseEntity.ok(options);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorOptionResponse>> doctors(
            @RequestParam(required = false) Long especialidadId) {
        List<DoctorOptionResponse> options = catalogUseCase.getDoctorsBySpecialty(especialidadId).stream()
                .map(d -> DoctorOptionResponse.builder()
                        .personalId(d.getPersonalId())
                        .nombreCompleto(d.getNombreCompleto())
                        .especialidadId(d.getEspecialidadId())
                        .numeroColegiado(d.getNumeroColegiado())
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

    private String toLabel(CareUnit unit) {
        return switch (unit) {
            case CONSULTORIO -> "Consultorio";
            case LABORATORIO -> "Laboratorio";
            case FARMACIA -> "Farmacia";
            case EMERGENCIA -> "Emergencia";
        };
    }
}

