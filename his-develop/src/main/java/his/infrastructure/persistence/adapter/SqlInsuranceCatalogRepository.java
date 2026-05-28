package his.infrastructure.persistence.adapter;

import his.domain.models.InsuranceCatalog;
import his.domain.ports.InsuranceCatalogRepository;
import his.infrastructure.persistence.mapper.InsuranceCatalogMapper;
import his.infrastructure.persistence.repositories.InsuranceCatalogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlInsuranceCatalogRepository implements InsuranceCatalogRepository {

    private final InsuranceCatalogJpaRepository insuranceCatalogJpaRepository;

    @Override
    public List<InsuranceCatalog> findAllActive() {
        return insuranceCatalogJpaRepository.findByIsActiveTrueOrderByNombreAseguradoraAsc()
                .stream()
                .map(InsuranceCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<InsuranceCatalog> findById(Long aseguradoraId) {
        return insuranceCatalogJpaRepository.findById(aseguradoraId)
                .filter(entity -> Boolean.TRUE.equals(entity.getIsActive()))
                .map(InsuranceCatalogMapper::toDomain);
    }
}