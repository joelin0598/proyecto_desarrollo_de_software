package his.infrastructure.persistence.mapper;

import his.domain.models.Patient;
import his.infrastructure.persistence.entities.PatientJpaEntity;
import his.infrastructure.persistence.entities.UserJpaEntity;

public final class PatientMapper {
    private PatientMapper() {
    }

    public static PatientJpaEntity toEntity(Patient domain, UserJpaEntity usuarioSistema) {
        if (domain == null) {
            return null;
        }
        PatientJpaEntity entity = PatientJpaEntity.builder()
                .pacienteId(domain.getPacienteId())
                .usuarioSistema(usuarioSistema)
                .nombreCompleto(domain.getNombreCompleto())
                .dpi(domain.getDpi())
                .build();
        entity.setIsActive(true);
        return entity;
    }

    public static Patient toDomain(PatientJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Patient.builder()
                .pacienteId(entity.getPacienteId())
                .usuarioId(entity.getUsuarioSistema().getUsuarioId())
                .nombreCompleto(entity.getNombreCompleto())
                .dpi(entity.getDpi())
                .build();
    }


}

