package his.infrastructure.persistence.adapters;

import his.domain.UserEntity;
import his.domain.model.Paciente;
import his.domain.ports.PacienteRepository;
import his.infrastructure.persistence.PacienteJpaEntity;
import his.infrastructure.persistence.PacienteJpaRepository;
import his.infrastructure.persistence.UsuarioSistemaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto {@link PacienteRepository}.
 * Traduce las operaciones de dominio en operaciones JPA sobre la tabla 'paciente'.
 */
@Repository
@RequiredArgsConstructor
public class SqlPacienteRepositoryAdapter implements PacienteRepository {

    private final PacienteJpaRepository jpaRepository;
    private final UsuarioSistemaJpaRepository usuarioJpaRepository;

    @Override
    public Paciente save(Paciente paciente) {
        UserEntity usuario = usuarioJpaRepository.findById(paciente.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario con id: " + paciente.getUsuarioId()));

        PacienteJpaEntity entity = PacienteJpaEntity.builder()
                .pacienteId(paciente.getPacienteId())
                .dpi(paciente.getDpi())
                .usuario(usuario)
                .build();

        PacienteJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Paciente> findByDpi(String dpi) {
        return jpaRepository.findByDpi(dpi).map(this::toDomain);
    }

    @Override
    public Optional<Paciente> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioUserId(usuarioId).map(this::toDomain);
    }

    private Paciente toDomain(PacienteJpaEntity entity) {
        return Paciente.builder()
                .pacienteId(entity.getPacienteId())
                .dpi(entity.getDpi())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getUserId() : null)
                .build();
    }
}
