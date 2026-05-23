package his.infrastructure.persistence.adapter;

import his.domain.models.LaboratoryResult;
import his.domain.ports.LaboratoryResultRepository;
import his.infrastructure.persistence.entities.LaboratoryResultJpaEntity;
import his.infrastructure.persistence.repositories.LaboratoryOrderJpaRepository;
import his.infrastructure.persistence.repositories.LaboratoryResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlLaboratoryResultRepository implements LaboratoryResultRepository {

    private final LaboratoryResultJpaRepository jpaRepository;
    private final LaboratoryOrderJpaRepository orderJpaRepository;

    @Override
    public LaboratoryResult save(LaboratoryResult result) {
        var orden = orderJpaRepository.getReferenceById(result.getOrdenLaboratorioId());
        var entity = LaboratoryResultJpaEntity.builder()
                .resultadoLaboratorioId(result.getResultadoLaboratorioId())
                .ordenLaboratorio(orden)
                .nombreExamen(result.getNombreExamen())
                .valorResultado(result.getValorResultado())
                .unidadResultado(result.getUnidadResultado())
                .referenciaMinima(result.getReferenciaMinima())
                .referenciaMaxima(result.getReferenciaMaxima())
                .observaciones(result.getObservaciones())
                .resumen(result.getResumen())
                .conclusion(result.getConclusion())
                .critico(result.isCritico())
                .build();
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LaboratoryResult> findByOrdenId(Long ordenLaboratorioId) {
        return jpaRepository
                .findTopByOrdenLaboratorioOrdenLaboratorioIdOrderByCreatedAtDesc(ordenLaboratorioId)
                .map(this::toDomain);
    }

    private LaboratoryResult toDomain(LaboratoryResultJpaEntity e) {
        return LaboratoryResult.builder()
                .resultadoLaboratorioId(e.getResultadoLaboratorioId())
                .ordenLaboratorioId(e.getOrdenLaboratorio().getOrdenLaboratorioId())
                .nombreExamen(e.getNombreExamen())
                .valorResultado(e.getValorResultado())
                .unidadResultado(e.getUnidadResultado())
                .referenciaMinima(e.getReferenciaMinima())
                .referenciaMaxima(e.getReferenciaMaxima())
                .observaciones(e.getObservaciones())
                .resumen(e.getResumen())
                .conclusion(e.getConclusion())
                .critico(Boolean.TRUE.equals(e.getCritico()))
                .createdAt(e.getCreatedAt())
                .build();
    }
}

