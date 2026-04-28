package his.infrastructure.persistence.adapters;

import his.domain.UserEntity;
import his.domain.model.PersonalHospitalario;
import his.domain.ports.PersonalHospitalarioRepository;
import his.infrastructure.persistence.PersonalHospitalarioJpaEntity;
import his.infrastructure.persistence.PersonalHospitalarioJpaRepository;
import his.infrastructure.persistence.UsuarioSistemaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto {@link PersonalHospitalarioRepository}.
 * Traduce las operaciones de dominio en operaciones JPA sobre la tabla 'personal_hospitalario'.
 */
@Repository
@RequiredArgsConstructor
public class SqlPersonalHospitalarioRepositoryAdapter implements PersonalHospitalarioRepository {

    private final PersonalHospitalarioJpaRepository jpaRepository;
    private final UsuarioSistemaJpaRepository usuarioJpaRepository;

    @Override
    public PersonalHospitalario save(PersonalHospitalario personal) {
        UserEntity usuario = usuarioJpaRepository.findById(personal.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario con id: " + personal.getUsuarioId()));

        PersonalHospitalarioJpaEntity entity = PersonalHospitalarioJpaEntity.builder()
                .personalId(personal.getPersonalId())
                .dpi(personal.getDpi())
                .direccion(personal.getDireccion())
                .numeroColegiado(personal.getNumeroColegiado())
                .rol(personal.getRol())
                .usuario(usuario)
                .build();

        PersonalHospitalarioJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PersonalHospitalario> findByDpi(String dpi) {
        return jpaRepository.findByDpi(dpi).map(this::toDomain);
    }

    @Override
    public Optional<PersonalHospitalario> findByUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioUserId(usuarioId).map(this::toDomain);
    }

    private PersonalHospitalario toDomain(PersonalHospitalarioJpaEntity entity) {
        return PersonalHospitalario.builder()
                .personalId(entity.getPersonalId())
                .dpi(entity.getDpi())
                .direccion(entity.getDireccion())
                .numeroColegiado(entity.getNumeroColegiado())
                .rol(entity.getRol())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getUserId() : null)
                .build();
    }
}
