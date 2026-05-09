package his.infrastructure.persistence.adapter;

import his.domain.models.VitalSigns;
import his.domain.ports.VitalSignsRepository;
import his.infrastructure.persistence.entities.HospitalStaffJpaEntity;
import his.infrastructure.persistence.entities.PatientJpaEntity;
import his.infrastructure.persistence.entities.VitalSignsJpaEntity;
import his.infrastructure.persistence.mapper.VitalSignsMapper;
import his.infrastructure.persistence.repositories.VitalSignsJpaRepository;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqlVitalSignsRepository implements VitalSignsRepository {
    private final VitalSignsJpaRepository jpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;
    private final VitalSignsMapper mapper;

    @Override
    public VitalSigns save(VitalSigns vitalSigns) {
        VitalSignsJpaEntity entity = mapper.toEntity(vitalSigns);

        if (vitalSigns.getPacienteId() != null) {
            PatientJpaEntity pacienteRef = patientJpaRepository.getReferenceById(vitalSigns.getPacienteId());
            entity.setPaciente(pacienteRef);
        }

        if (vitalSigns.getPersonalId() != null) {
            HospitalStaffJpaEntity personalRef = hospitalStaffJpaRepository.getReferenceById(vitalSigns.getPersonalId());
            entity.setPersonal(personalRef);
        }

        VitalSignsJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
