package his.infrastructure.persistence.mapper;

import his.domain.models.MedicalAppointment;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import his.infrastructure.persistence.entities.MedicalAppointmentJpaEntity;
import his.infrastructure.persistence.entities.PatientJpaEntity;

public final class MedicalAppointmentMapper {

    private MedicalAppointmentMapper() {
    }

    public static MedicalAppointmentJpaEntity toEntity(
            MedicalAppointment domain,
            PatientJpaEntity paciente,
            HospitalStaffJpaEntity personal
    ) {
        return MedicalAppointmentJpaEntity.builder()
                .citaMedicaId(domain.getCitaMedicaId())
                .paciente(paciente)
                .personal(personal)
                .especialidadId(domain.getEspecialidadId())
                .fechaCita(domain.getFechaCita())
                .horaCita(domain.getHoraCita())
                .motivoConsulta(domain.getMotivoConsulta())
                .metodoPago(domain.getMetodoPago())
                .costoConsulta(domain.getCostoConsulta())
                .estadoCita(domain.getEstadoCita())
                .estadoAdministrativo(domain.getEstadoAdministrativo())
                .observacionAdministrativa(domain.getObservacionAdministrativa())
                .build();
    }

    public static MedicalAppointment toDomain(MedicalAppointmentJpaEntity entity) {
        return MedicalAppointment.builder()
                .citaMedicaId(entity.getCitaMedicaId())
                .pacienteId(entity.getPaciente().getPacienteId())
                .personalId(entity.getPersonal().getPersonalId())
                .especialidadId(entity.getEspecialidadId())
                .fechaCita(entity.getFechaCita())
                .horaCita(entity.getHoraCita())
                .motivoConsulta(entity.getMotivoConsulta())
                .metodoPago(entity.getMetodoPago())
                .costoConsulta(entity.getCostoConsulta())
                .estadoCita(entity.getEstadoCita())
                .estadoAdministrativo(entity.getEstadoAdministrativo())
                .observacionAdministrativa(entity.getObservacionAdministrativa())
                .build();
    }
}
