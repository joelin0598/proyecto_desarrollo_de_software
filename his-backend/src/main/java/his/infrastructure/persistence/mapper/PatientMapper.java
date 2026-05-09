package his.infrastructure.persistence.mapper;

import his.domain.models.Patient;
import his.infrastructure.persistence.entities.InsuranceCatalogJpaEntity;
import his.infrastructure.persistence.entities.PatientJpaEntity;
import his.infrastructure.persistence.entities.UserJpaEntity;

public final class PatientMapper {
    private PatientMapper() {
    }

    /**
     * Convierte dominio Patient -> JPA.
     * @param domain       modelo de dominio
     * @param usuarioSistema referencia JPA del usuario; puede ser null para pacientes de triaje presencial
     * @param aseguradora    referencia JPA de la aseguradora; puede ser null si no aplica seguro
     */
    public static PatientJpaEntity toEntity(Patient domain,
                                            UserJpaEntity usuarioSistema,
                                            InsuranceCatalogJpaEntity aseguradora) {
        if (domain == null) {
            return null;
        }
        PatientJpaEntity entity = PatientJpaEntity.builder()
                .pacienteId(domain.getPacienteId())
                .usuarioSistema(usuarioSistema)           // nullable
                .nombreCompleto(domain.getNombreCompleto())
                .dpi(domain.getDpi())
                .genero(domain.getGenero())
                .aseguradora(aseguradora)                 // nullable
                .polizaSeguro(domain.getPolizaSeguro())
                .fechaNacimiento(domain.getFechaNacimiento())
                .emailContacto(domain.getEmailContacto())
                .direccion(domain.getDireccion())
                .telefono(domain.getTelefono())
                .contactoEmergencia(domain.getContactoEmergencia())
                .telefonoEmergencia(domain.getTelefonoEmergencia())
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
                .usuarioId(entity.getUsuarioSistema() != null
                        ? entity.getUsuarioSistema().getUsuarioId()
                        : null)
                .nombreCompleto(entity.getNombreCompleto())
                .dpi(entity.getDpi())
                .genero(entity.getGenero())
                .aseguradoraId(entity.getAseguradora() != null
                        ? entity.getAseguradora().getAseguradoraId()
                        : null)
                .polizaSeguro(entity.getPolizaSeguro())
                .fechaNacimiento(entity.getFechaNacimiento())
                .emailContacto(entity.getEmailContacto())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .contactoEmergencia(entity.getContactoEmergencia())
                .telefonoEmergencia(entity.getTelefonoEmergencia())
                .build();
    }
}
