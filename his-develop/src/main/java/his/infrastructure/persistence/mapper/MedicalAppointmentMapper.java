package his.infrastructure.persistence.mapper;

import his.domain.models.MedicalAppointment;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import his.infrastructure.persistence.entities.MedicalAppointmentJpaEntity;
import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import his.infrastructure.persistence.entities.PatientJpaEntity;

public final class MedicalAppointmentMapper {

    private MedicalAppointmentMapper() {
    }

    public static MedicalAppointmentJpaEntity toEntity(
            MedicalAppointment domain,
            PatientJpaEntity paciente,
            HospitalStaffJpaEntity personal,
            MedicalSpecialityCatalogJpaEntity especialidad
    ) {
        return MedicalAppointmentJpaEntity.builder()
                .citaMedicaId(domain.getCitaMedicaId())
                .paciente(paciente)
                .personal(personal)
                .especialidad(especialidad)
                .fechaCita(domain.getFechaCita())
                .horaCita(domain.getHoraCita())
                .motivoConsulta(domain.getMotivoConsulta())
                .metodoPago(domain.getMetodoPago())
                .costoConsulta(domain.getCostoConsulta())
                .estadoCita(domain.getEstadoCita())
                .estadoAdministrativo(domain.getEstadoAdministrativo())
                .observacionAdministrativa(domain.getObservacionAdministrativa())
                .solvenciaPago(Boolean.TRUE.equals(domain.getSolvenciaPago()))
                .citaProgramada(Boolean.TRUE.equals(domain.getCitaProgramada()))
                .codigoCita(domain.getCodigoCita())
                .qrContenido(domain.getQrContenido())
                .presionSistolica(domain.getPresionSistolica())
                .presionDiastolica(domain.getPresionDiastolica())
                .frecuenciaCardiaca(domain.getFrecuenciaCardiaca())
                .temperatura(domain.getTemperatura())
                .saturacionOxigeno(domain.getSaturacionOxigeno())
                .tallaCm(domain.getTallaCm())
                .pesoKg(domain.getPesoKg())
                .prioridad(domain.getPrioridad())
                .alertaEmergencia(domain.getAlertaEmergencia())
                .fechaHoraTriaje(domain.getFechaHoraTriaje())
                .build();
    }

    public static MedicalAppointment toDomain(MedicalAppointmentJpaEntity entity) {
        return MedicalAppointment.builder()
                .citaMedicaId(entity.getCitaMedicaId())
                .pacienteId(entity.getPaciente().getPacienteId())
                .personalId(entity.getPersonal() != null ? entity.getPersonal().getPersonalId() : null)
                .especialidadId(entity.getEspecialidad() != null ? entity.getEspecialidad().getEspecialidadId() : null)
                .fechaCita(entity.getFechaCita())
                .horaCita(entity.getHoraCita())
                .motivoConsulta(entity.getMotivoConsulta())
                .metodoPago(entity.getMetodoPago())
                .costoConsulta(entity.getCostoConsulta())
                .estadoCita(entity.getEstadoCita())
                .estadoAdministrativo(entity.getEstadoAdministrativo())
                .observacionAdministrativa(entity.getObservacionAdministrativa())
                .solvenciaPago(entity.getSolvenciaPago())
                .citaProgramada(entity.getCitaProgramada())
                .codigoCita(entity.getCodigoCita())
                .qrContenido(entity.getQrContenido())
                .presionSistolica(entity.getPresionSistolica())
                .presionDiastolica(entity.getPresionDiastolica())
                .frecuenciaCardiaca(entity.getFrecuenciaCardiaca())
                .temperatura(entity.getTemperatura())
                .saturacionOxigeno(entity.getSaturacionOxigeno())
                .tallaCm(entity.getTallaCm())
                .pesoKg(entity.getPesoKg())
                .prioridad(entity.getPrioridad())
                .alertaEmergencia(entity.getAlertaEmergencia())
                .fechaHoraTriaje(entity.getFechaHoraTriaje())
                .build();
    }
}
