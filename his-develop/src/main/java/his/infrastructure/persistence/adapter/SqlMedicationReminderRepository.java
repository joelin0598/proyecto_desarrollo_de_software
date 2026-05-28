package his.infrastructure.persistence.adapter;

import his.domain.models.MedicationReminder;
import his.domain.ports.MedicationReminderRepository;
import his.infrastructure.persistence.entities.MedicationReminderJpaEntity;
import his.infrastructure.persistence.repositories.MedicalPrescriptionDetailsJpaRepository;
import his.infrastructure.persistence.repositories.MedicationReminderJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SqlMedicationReminderRepository implements MedicationReminderRepository {

    private final MedicationReminderJpaRepository jpaRepository;
    private final MedicalPrescriptionDetailsJpaRepository detailsJpaRepository;
    private final PatientJpaRepository patientJpaRepository;

    @Override
    public MedicationReminder save(MedicationReminder r) {
        var detalle = detailsJpaRepository.getReferenceById(r.getRecetaMedicaDetalleId());
        var paciente = patientJpaRepository.getReferenceById(r.getPacienteId());
        var entity = MedicationReminderJpaEntity.builder()
                .recordatorioId(r.getRecordatorioId())
                .recetaMedicaDetalle(detalle)
                .paciente(paciente)
                .medicamentoNombre(r.getMedicamentoNombre())
                .dosis(r.getDosis())
                .frecuenciaHoras(r.getFrecuenciaHoras())
                .duracionDias(r.getDuracionDias())
                .viaAdministracion(r.getViaAdministracion())
                .proximoRecordatorio(r.getProximoRecordatorio())
                .activo(r.isActivo())
                .build();
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<MedicationReminder> findActivosByPacienteId(Long pacienteId) {
        return jpaRepository
                .findByPacientePacienteIdAndActivoTrueOrderByProximoRecordatorioAsc(pacienteId)
                .stream().map(this::toDomain).toList();
    }

    private MedicationReminder toDomain(MedicationReminderJpaEntity e) {
        return MedicationReminder.builder()
                .recordatorioId(e.getRecordatorioId())
                .recetaMedicaDetalleId(e.getRecetaMedicaDetalle().getRecetaMedicaDetalleId())
                .pacienteId(e.getPaciente().getPacienteId())
                .medicamentoNombre(e.getMedicamentoNombre())
                .dosis(e.getDosis())
                .frecuenciaHoras(e.getFrecuenciaHoras())
                .duracionDias(e.getDuracionDias())
                .viaAdministracion(e.getViaAdministracion())
                .proximoRecordatorio(e.getProximoRecordatorio())
                .activo(Boolean.TRUE.equals(e.getActivo()))
                .build();
    }
}

