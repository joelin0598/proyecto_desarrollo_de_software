package his.infrastructure.persistence.adapter;

import his.domain.models.MedicalSpecialityCatalog;
import his.domain.ports.MedicalSpecialityRepository;
import his.infrastructure.persistence.repositories.MedicalSpecialityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalSpecialityRepository implements MedicalSpecialityRepository {

    private final MedicalSpecialityJpaRepository jpaRepository;

    @Override
    public List<MedicalSpecialityCatalog> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByNombreAsc().stream()
                .map(e -> MedicalSpecialityCatalog.builder()
                        .especialidadMedicaId(e.getEspecialidadId())
                        .nombre(e.getNombre())
                        .descripcion(e.getDescripcion())
                        .build())
                .toList();
    }

    @Override
    public Optional<MedicalSpecialityCatalog> findById(Long especialidadId) {
        return jpaRepository.findById(especialidadId)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(e -> MedicalSpecialityCatalog.builder()
                        .especialidadMedicaId(e.getEspecialidadId())
                        .nombre(e.getNombre())
                        .descripcion(e.getDescripcion())
                        .build());
    }
}