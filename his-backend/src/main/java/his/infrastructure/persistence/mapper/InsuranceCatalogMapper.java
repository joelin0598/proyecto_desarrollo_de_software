package his.infrastructure.persistence.mapper;

import his.domain.models.InsuranceCatalog;
import his.infrastructure.persistence.entities.InsuranceCatalogJpaEntity;

public final class InsuranceCatalogMapper {
    private InsuranceCatalogMapper() {
    }

    public static InsuranceCatalog toDomain(InsuranceCatalogJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return InsuranceCatalog.builder()
                .aseguradoraId(entity.getAseguradoraId())
                .nombre(entity.getNombreAseguradora())
                .descripcion(entity.getDescripcion())
                .polizaSeguro(entity.getPolizaSeguro())
                .build();
    }
}

