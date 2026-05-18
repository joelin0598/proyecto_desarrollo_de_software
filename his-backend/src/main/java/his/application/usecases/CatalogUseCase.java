package his.application.usecases;

import his.domain.models.HospitalStaff;
import his.domain.models.InsuranceCatalog;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.PatientGender;

import java.util.List;

public interface CatalogUseCase {
    List<PatientGender> getPatientGenders();

    List<InsuranceCatalog> getActiveInsurances();

    List<MedicalSpecialityCatalog> getActiveSpecialties();

    List<HospitalStaff> getDoctorsBySpecialty(Long especialidadId);
}

