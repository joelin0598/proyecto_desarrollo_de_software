package his.infrastructure.persistence.mapper;

import his.domain.models.HospitalStaff;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import his.infrastructure.persistence.entities.UserJpaEntity;

public final class HospitalStaffMapper {
    private HospitalStaffMapper() {
    }

    public static HospitalStaffJpaEntity toEntity(HospitalStaff domain,
                                                  UserJpaEntity usuarioSistema,
                                                  MedicalSpecialityCatalogJpaEntity especialidad) {
        if (domain == null) {
            return null;
        }
        HospitalStaffJpaEntity entity = HospitalStaffJpaEntity.builder()
                .personalId(domain.getPersonalId())
                .usuarioSistema(usuarioSistema)
                .rol(domain.getRol())
                .especialidad(especialidad)
                .unidadAtencionId(domain.getUnidadAtencionId())
                .numeroColejiado(domain.getNumeroColegiado())
                .telefonoCorporativo(domain.getTelefonoCorporativo())
                .nombreCompleto(domain.getNombreCompleto())
                .direccion(domain.getDireccion())
                .build();
        entity.setIsActive(true);
        return entity;
    }

    public static HospitalStaff toDomain(HospitalStaffJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return HospitalStaff.builder()
                .personalId(entity.getPersonalId())
                .usuarioId(entity.getUsuarioSistema().getUsuarioId())
                .rol(entity.getRol())
                .especialidadId(entity.getEspecialidad() != null ? entity.getEspecialidad().getEspecialidadId() : null)
                .unidadAtencionId(entity.getUnidadAtencionId())
                .numeroColegiado(entity.getNumeroColejiado())
                .telefonoCorporativo(entity.getTelefonoCorporativo())
                .nombreCompleto(entity.getNombreCompleto())
                .direccion(entity.getDireccion())
                .build();
    }


}

