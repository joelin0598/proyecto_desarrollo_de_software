package his.infrastructure.persistence.adapter;

import his.domain.models.Patient;
import his.domain.ports.PatientRepository;
import his.infrastructure.persistence.entities.InsuranceCatalogJpaEntity;
import his.infrastructure.persistence.entities.UserJpaEntity;
import his.infrastructure.persistence.mapper.PatientMapper;
import his.infrastructure.persistence.repositories.InsuranceCatalogJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import his.infrastructure.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlPatientRepository implements PatientRepository {

    private final PatientJpaRepository patientJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final InsuranceCatalogJpaRepository insuranceCatalogJpaRepository;

    @Override
    public Patient save(Patient patient) {
        // usuarioSistema es nullable: pacientes de triaje presencial no tienen cuenta web
        UserJpaEntity usuario = null;
        if (patient.getUsuarioId() != null) {
            usuario = userJpaRepository.findById(patient.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe usuario_sistema para usuarioId=" + patient.getUsuarioId()));
        }

        // aseguradora es nullable: seguro es opcional
        InsuranceCatalogJpaEntity aseguradora = null;
        if (patient.getAseguradoraId() != null) {
            aseguradora = insuranceCatalogJpaRepository.getReferenceById(patient.getAseguradoraId());
        }

        var saved = patientJpaRepository.save(PatientMapper.toEntity(patient, usuario, aseguradora));
        return PatientMapper.toDomain(saved);
    }

    @Override
    public Optional<Patient> findByUsuarioId(Long usuarioId) {
        return patientJpaRepository.findByUsuarioSistemaUsuarioId(usuarioId)
                .map(PatientMapper::toDomain);
    }

    @Override
    public Optional<Patient> findByDpi(String dpi) {
        if (dpi == null || dpi.isBlank()) {
            return Optional.empty();
        }
        return patientJpaRepository.findByDpi(dpi)
                .map(PatientMapper::toDomain);
    }

    @Override
    public Optional<Patient> findById(Long pacienteId) {
        return patientJpaRepository.findById(pacienteId)
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
