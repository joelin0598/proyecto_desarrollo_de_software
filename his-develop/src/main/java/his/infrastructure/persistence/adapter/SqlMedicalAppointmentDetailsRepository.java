package his.infrastructure.persistence.adapter;

import his.domain.models.MedicalAppointmentDetails;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.infrastructure.persistence.entities.MedicalAppointmentDetailsJpaEntity;
import his.infrastructure.persistence.repositories.MedicalAppointmentDetailsJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalAppointmentDetailsRepository implements MedicalAppointmentDetailsRepository {

    private final MedicalAppointmentDetailsJpaRepository detailsJpaRepository;
    private final MedicalAppointmentJpaRepository appointmentJpaRepository;

    @Override
    public MedicalAppointmentDetails save(MedicalAppointmentDetails details) {
        var cita = appointmentJpaRepository.getReferenceById(details.getCitaMedicaId());
        var citaSeguimiento = details.getCitaSeguimientoId() != null
                ? appointmentJpaRepository.getReferenceById(details.getCitaSeguimientoId())
                : null;
        var entity = MedicalAppointmentDetailsJpaEntity.builder()
                .citaMedicaDetalleId(details.getMedicalAppointmentDetailsId())
                .citaMedica(cita)
                .evaluacionFisica(details.getEvaluacionFisica())
                .diagnostico(details.getDiagnostico())
                .ordenLaboratorio(details.getOrdenLaboratorio())
                .recetaMedica(details.getRecetaMedica())
                .medicacionPrescrita(details.getMedicacionPrescrita())
                .requiereSeguimiento(details.getRequiereSeguimiento())
                .citaSeguimiento(citaSeguimiento)
                .build();

        var saved = detailsJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MedicalAppointmentDetails> findById(Long medicalAppointmentDetailsId) {
        return detailsJpaRepository.findById(medicalAppointmentDetailsId).map(this::toDomain);
    }

    @Override
    public Optional<MedicalAppointmentDetails> findByCitaMedicaId(Long citaMedicaId) {
        return detailsJpaRepository.findTopByCitaMedicaCitaMedicaIdOrderByCreatedAtDesc(citaMedicaId)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long medicalAppointmentDetailsId) {
        detailsJpaRepository.deleteById(medicalAppointmentDetailsId);
    }

    private MedicalAppointmentDetails toDomain(MedicalAppointmentDetailsJpaEntity entity) {
        return MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(entity.getCitaMedicaDetalleId())
                .citaMedicaId(entity.getCitaMedica().getCitaMedicaId())
                .evaluacionFisica(entity.getEvaluacionFisica())
                .diagnostico(entity.getDiagnostico())
                .ordenLaboratorio(entity.getOrdenLaboratorio())
                .recetaMedica(entity.getRecetaMedica())
                .medicacionPrescrita(entity.getMedicacionPrescrita())
                .requiereSeguimiento(entity.getRequiereSeguimiento())
                .citaSeguimientoId(entity.getCitaSeguimiento() != null ? entity.getCitaSeguimiento().getCitaMedicaId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

