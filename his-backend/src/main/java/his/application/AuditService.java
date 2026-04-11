package his.application;

import his.domain.AuditLog;
import his.domain.ports.AuditLogRepository;
import his.infrastructure.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de auditoría inmutable (RN05).
 * Registra cada acción importante del sistema en la bitácora de auditoría.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Registra una acción en la bitácora de auditoría.
     *
     * @param action     Nombre de la acción realizada (ej. "REGISTER_PATIENT")
     * @param entityType Tipo de entidad afectada (ej. "Patient")
     * @param entityId   ID de la entidad afectada
     * @param details    Descripción detallada de la acción
     */
    public void log(String action, String entityType, Long entityId, String details) {
        try {
            String currentUser = SecurityUtil.getCurrentUser();
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .performedBy(currentUser)
                    .build();
            auditLogRepository.save(entry);
            log.info("Audit: {} on {} id={} by {}", action, entityType, entityId, currentUser);
        } catch (Exception e) {
            log.error("Failed to save audit log entry: {}", e.getMessage(), e);
        }
    }
}
