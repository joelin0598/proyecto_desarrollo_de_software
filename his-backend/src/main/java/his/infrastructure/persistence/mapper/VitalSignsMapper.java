package his.infrastructure.persistence.mapper;


import his.domain.models.VitalSigns;
import his.infrastructure.persistence.entities.VitalSignsJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VitalSignsMapper {

    public VitalSignsJpaEntity toEntity(VitalSigns domain) {
        if(domain == null) return null;
        return VitalSignsJpaEntity.builder()
                .signosVitalesId(domain.getSignosVitalesId())
                .presionSistolica(domain.getPresionSistolica())
                .presionDiastolica(domain.getPresionDiastolica())
                .frecuenciaCardiaca(domain.getFrecuenciaCardiaca())
                .temperatura(domain.getTemperatura())
                .saturacionOxigeno(domain.getSaturacionOxigeno())
                .tallaCm(domain.getTallaCm())
                .pesoKg(domain.getPesoKg())
                .priority(domain.getPriority())
                .build();
    }

    public VitalSigns toDomain(VitalSignsJpaEntity entity){
        if(entity == null) return null;
        return VitalSigns.builder()
                .signosVitalesId(entity.getSignosVitalesId())
                .citaMedicaId(entity.getCitaMedica() != null ? entity.getCitaMedica().getCitaMedicaId() : null)
                .pacienteId(entity.getPaciente() != null ? entity.getPaciente().getPacienteId() : null)
                .personalId(entity.getPersonal() != null ? entity.getPersonal().getPersonalId() : null)
                .presionSistolica(entity.getPresionSistolica())
                .presionDiastolica(entity.getPresionDiastolica())
                .frecuenciaCardiaca(entity.getFrecuenciaCardiaca())
                .temperatura(entity.getTemperatura())
                .saturacionOxigeno(entity.getSaturacionOxigeno())
                .tallaCm(entity.getTallaCm())
                .pesoKg(entity.getPesoKg())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .build();

    }
}
