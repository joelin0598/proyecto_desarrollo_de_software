package his.infrastructure.persistence.adapter;

import his.domain.models.Patient;
import his.domain.ports.PatientRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import his.infrastructure.persistence.repositories.UserJpaRepository;
import his.infrastructure.persistence.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlPatientRepository implements PatientRepository {

    private final PatientJpaRepository patientJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public Patient save(Patient patient) {
        var usuario = userJpaRepository.findById(patient.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario_sistema para usuarioId=" + patient.getUsuarioId()));

        var saved = patientJpaRepository.save(PatientMapper.toEntity(patient, usuario));
        return PatientMapper.toDomain(saved);
    }

    @Override
    public Optional<Patient> findByUsuarioId(Long usuarioId) {
        return patientJpaRepository.findByUsuarioSistemaUsuarioId(usuarioId)
                .map(PatientMapper::toDomain);
    }

    @Override
    public boolean existsByDpi(String dpi) {
        if (dpi == null || dpi.isBlank()) {
            return false;
        }
        return patientJpaRepository.existsByDpi(dpi);
    }
}

