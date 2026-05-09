package his.application.usecases;

import his.domain.models.InsuranceCatalog;
import his.domain.models.PatientGender;

import java.util.List;

public interface CatalogUseCase {
    List<PatientGender> getPatientGenders();

    List<InsuranceCatalog> getActiveInsurances();
}

