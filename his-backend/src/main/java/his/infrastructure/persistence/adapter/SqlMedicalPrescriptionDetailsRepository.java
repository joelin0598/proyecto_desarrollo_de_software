package his.infrastructure.persistence.adapter;

import his.domain.models.MedicalPrescriptionDetails;
import his.domain.ports.MedicalPrescriptionDetailsRepository;
import his.infrastructure.persistence.entities.MedicalPrescriptionDetailsJpaEntity;
import his.infrastructure.persistence.repositories.MedicalPrescriptionDetailsJpaRepository;
import his.infrastructure.persistence.repositories.MedicalPrescriptionJpaRepository;
import his.infrastructure.persistence.repositories.MedicineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalPrescriptionDetailsRepository implements MedicalPrescriptionDetailsRepository {

    private final MedicalPrescriptionDetailsJpaRepository jpaRepository;
    private final MedicalPrescriptionJpaRepository prescriptionJpaRepository;
    private final MedicineJpaRepository medicineJpaRepository;

    @Override
    public MedicalPrescriptionDetails save(MedicalPrescriptionDetails d) {
        var receta = prescriptionJpaRepository.getReferenceById(d.getRecetaMedicaId());
        var medicamento = medicineJpaRepository.getReferenceById(d.getMedicamentoId());
        var entity = MedicalPrescriptionDetailsJpaEntity.builder()
                .recetaMedicaDetalleId(d.getRecetaMedicaDetalleId())
                .recetaMedica(receta)
                .medicamento(medicamento)
                .cantidad(d.getCantidad())
                .dosis(d.getDosis())
                .viaAdministracion(d.getViaAdministracion())
                .frecuenciaHoras(d.getFrecuenciaHoras())
                .duracionDias(d.getDuracionDias())
                .despachado(d.isDespachado())
                .pagoValidado(d.isPagoValidado())
                .build();
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MedicalPrescriptionDetails> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MedicalPrescriptionDetails> findByRecetaId(Long recetaMedicaId) {
        return jpaRepository.findByRecetaMedicaRecetaMedicaIdOrderByCreatedAtAsc(recetaMedicaId)
                .stream().map(this::toDomain).toList();
    }

    private MedicalPrescriptionDetails toDomain(MedicalPrescriptionDetailsJpaEntity e) {
        return MedicalPrescriptionDetails.builder()
                .recetaMedicaDetalleId(e.getRecetaMedicaDetalleId())
                .recetaMedicaId(e.getRecetaMedica().getRecetaMedicaId())
                .medicamentoId(e.getMedicamento().getMedicamentoId())
                .medicamentoNombre(e.getMedicamento().getNombre())
                .cantidad(e.getCantidad() != null ? e.getCantidad() : 0)
                .dosis(e.getDosis())
                .viaAdministracion(e.getViaAdministracion())
                .frecuenciaHoras(e.getFrecuenciaHoras())
                .duracionDias(e.getDuracionDias())
                .despachado(Boolean.TRUE.equals(e.getDespachado()))
                .pagoValidado(Boolean.TRUE.equals(e.getPagoValidado()))
                .build();
    }
}

