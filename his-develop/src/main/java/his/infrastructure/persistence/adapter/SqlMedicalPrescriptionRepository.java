package his.infrastructure.persistence.adapter;

import his.domain.models.MedicalPrescription;
import his.domain.ports.MedicalPrescriptionRepository;
import his.infrastructure.persistence.entities.MedicalPrescriptionJpaEntity;
import his.infrastructure.persistence.repositories.MedicalAppointmentDetailsJpaRepository;
import his.infrastructure.persistence.repositories.MedicalPrescriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalPrescriptionRepository implements MedicalPrescriptionRepository {

    private final MedicalPrescriptionJpaRepository jpaRepository;
    private final MedicalAppointmentDetailsJpaRepository detalleJpaRepository;

    @Override
    public MedicalPrescription save(MedicalPrescription p) {
        var detalle = detalleJpaRepository.getReferenceById(p.getCitaMedicaDetalleId());
        var entity = MedicalPrescriptionJpaEntity.builder()
                .recetaMedicaId(p.getRecetaMedicaId())
                .citaMedicaDetalle(detalle)
                .instruccionesGenerales(p.getInstruccionesGenerales())
                .fechaEmision(p.getFechaEmision())
                .build();
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MedicalPrescription> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<MedicalPrescription> findByCitaMedicaDetalleId(Long citaMedicaDetalleId) {
        return jpaRepository
                .findTopByCitaMedicaDetalleCitaMedicaDetalleIdOrderByCreatedAtDesc(citaMedicaDetalleId)
                .map(this::toDomain);
    }

    @Override
    public List<MedicalPrescription> findByPacienteDpi(String dpi) {
        return jpaRepository.findByPacienteDpiOrderByCreatedAtDesc(dpi)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MedicalPrescription toDomain(MedicalPrescriptionJpaEntity e) {
        return MedicalPrescription.builder()
                .recetaMedicaId(e.getRecetaMedicaId())
                .citaMedicaDetalleId(e.getCitaMedicaDetalle().getCitaMedicaDetalleId())
                .instruccionesGenerales(e.getInstruccionesGenerales())
                .fechaEmision(e.getFechaEmision())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

