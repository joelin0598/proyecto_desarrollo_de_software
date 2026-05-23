package his.infrastructure.persistence.adapter;

import his.domain.models.LaboratoryOrder;
import his.domain.models.LaboratoryOrderStatus;
import his.domain.ports.LaboratoryOrderRepository;
import his.infrastructure.persistence.entities.LaboratoryOrderJpaEntity;
import his.infrastructure.persistence.repositories.LaboratoryOrderJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentDetailsJpaRepository;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlLaboratoryOrderRepository implements LaboratoryOrderRepository {

    private final LaboratoryOrderJpaRepository jpaRepository;
    private final MedicalAppointmentDetailsJpaRepository detalleJpaRepository;
    private final HospitalStaffJpaRepository staffJpaRepository;

    @Override
    public LaboratoryOrder save(LaboratoryOrder order) {
        var detalle = detalleJpaRepository.getReferenceById(order.getCitaMedicaDetalleId());
        var personal = order.getPersonalId() != null
                ? staffJpaRepository.getReferenceById(order.getPersonalId()) : null;

        var entity = LaboratoryOrderJpaEntity.builder()
                .ordenLaboratorioId(order.getOrdenLaboratorioId())
                .citaMedicaDetalle(detalle)
                .personal(personal)
                .nombreExamen(order.getNombreExamen())
                .tipoMuestra(order.getTipoMuestra())
                .estado(order.getEstado() != null ? order.getEstado() : LaboratoryOrderStatus.PENDIENTE_PAGO)
                .pagoValidado(order.isPagoValidado())
                .etiquetaId(order.getEtiquetaId())
                .alertaCritica(order.isAlertaCritica())
                .observacionesTecnico(order.getObservacionesTecnico())
                .build();

        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LaboratoryOrder> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<LaboratoryOrder> findByCitaMedicaDetalleId(Long citaMedicaDetalleId) {
        return jpaRepository
                .findByCitaMedicaDetalleCitaMedicaDetalleIdOrderByCreatedAtDesc(citaMedicaDetalleId)
                .stream().map(this::toDomain).toList();
    }

    private LaboratoryOrder toDomain(LaboratoryOrderJpaEntity e) {
        return LaboratoryOrder.builder()
                .ordenLaboratorioId(e.getOrdenLaboratorioId())
                .citaMedicaDetalleId(e.getCitaMedicaDetalle().getCitaMedicaDetalleId())
                .personalId(e.getPersonal() != null ? e.getPersonal().getPersonalId() : null)
                .nombreExamen(e.getNombreExamen())
                .tipoMuestra(e.getTipoMuestra())
                .estado(e.getEstado())
                .pagoValidado(Boolean.TRUE.equals(e.getPagoValidado()))
                .etiquetaId(e.getEtiquetaId())
                .alertaCritica(Boolean.TRUE.equals(e.getAlertaCritica()))
                .observacionesTecnico(e.getObservacionesTecnico())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

