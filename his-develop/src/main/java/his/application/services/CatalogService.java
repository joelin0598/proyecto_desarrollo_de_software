package his.application.services;

import his.application.usecases.CatalogUseCase;
import his.domain.models.CareUnit;
import his.domain.models.HospitalStaff;
import his.domain.models.InsuranceCatalog;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.PatientGender;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.InsuranceCatalogRepository;
import his.domain.ports.MedicalSpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {

    private final InsuranceCatalogRepository insuranceCatalogRepository;
    private final MedicalSpecialityRepository specialtyCatalogRepository;
    private final HospitalStaffRepository hospitalStaffRepository;

    @Override
    public List<PatientGender> getPatientGenders() {
        return Arrays.asList(PatientGender.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceCatalog> getActiveInsurances() {
        return insuranceCatalogRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalSpecialityCatalog> getActiveSpecialties() {
        return specialtyCatalogRepository.findAllActive();
    }

    @Override
    public List<CareUnit> getCareUnits() {
        return List.of(CareUnit.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalStaff> getDoctorsBySpecialty(Long especialidadId) {
        if (especialidadId == null) {
            return hospitalStaffRepository.findAllDoctors();
        }
        return hospitalStaffRepository.findDoctorsByEspecialidadId(especialidadId);
    }
}

