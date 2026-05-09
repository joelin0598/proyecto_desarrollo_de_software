package his.infrastructure.persistence.mapper;

import his.domain.models.User;
import his.infrastructure.persistence.entities.UserJpaEntity;

public final class UserMapper {
    private UserMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .userId(entity.getUsuarioId())
                .email(entity.getEmailPaciente())
                .password(entity.getPasswordUsuario())
                .role(entity.getRol())
                .active(Boolean.TRUE.equals(entity.getIsActive()))
                .build();
    }

    public static UserJpaEntity toJpa(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = UserJpaEntity.builder()
                .usuarioId(domain.getUserId())
                .emailPaciente(domain.getEmail())
                .passwordUsuario(domain.getPassword())
                .rol(domain.getRole())
                .build();
        entity.setIsActive(domain.isActive());
        return entity;
    }
}

