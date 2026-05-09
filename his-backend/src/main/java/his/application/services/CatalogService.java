package his.application.services;

import his.application.usecases.CatalogUseCase;
import his.domain.models.InsuranceCatalog;
import his.domain.models.PatientGender;
import his.domain.ports.InsuranceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {

    private final InsuranceCatalogRepository insuranceCatalogRepository;

    @Override
    public List<PatientGender> getPatientGenders() {
        return Arrays.asList(PatientGender.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceCatalog> getActiveInsurances() {
        return insuranceCatalogRepository.findAllActive();
    }
}

