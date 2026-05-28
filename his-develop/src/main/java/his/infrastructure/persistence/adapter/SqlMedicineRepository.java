package his.infrastructure.persistence.adapter;

import his.domain.models.Medicine;
import his.domain.ports.MedicineRepository;
import his.infrastructure.persistence.entities.MedicineJpaEntity;
import his.infrastructure.persistence.repositories.MedicineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicineRepository implements MedicineRepository {

    private final MedicineJpaRepository jpaRepository;

    @Override
    public Optional<Medicine> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Medicine> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByNombreAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Medicine save(Medicine medicine) {
        var entity = jpaRepository.findById(medicine.getMedicamentoId())
                .orElse(MedicineJpaEntity.builder().build());
        entity.setMedicamentoId(medicine.getMedicamentoId());
        entity.setNombre(medicine.getNombre());
        entity.setPresentacion(medicine.getPresentacion());
        entity.setDescripcion(medicine.getDescripcion());
        entity.setStockActual(medicine.getStockActual());
        entity.setPrecioUnitario(medicine.getPrecioUnitario());
        return toDomain(jpaRepository.save(entity));
    }

    private Medicine toDomain(MedicineJpaEntity e) {
        return Medicine.builder()
                .medicamentoId(e.getMedicamentoId())
                .nombre(e.getNombre())
                .presentacion(e.getPresentacion())
                .descripcion(e.getDescripcion())
                .stockActual(e.getStockActual() != null ? e.getStockActual() : 0)
                .precioUnitario(e.getPrecioUnitario())
                .build();
    }
}

