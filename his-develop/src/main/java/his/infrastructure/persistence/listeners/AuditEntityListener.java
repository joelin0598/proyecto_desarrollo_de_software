package his.infrastructure.persistence.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import his.infrastructure.config.SpringContext;
import his.infrastructure.persistence.entities.ActivityLogJpaEntity;
import his.infrastructure.persistence.entities.TransactionLogJpaEntity;
import his.infrastructure.persistence.repositories.ActivityLogRepository;
import his.infrastructure.persistence.repositories.TransactionLogRepository;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.time.LocalDateTime;

@Slf4j
public class AuditEntityListener {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final Map<Object, String> SNAPSHOTS = Collections.synchronizedMap(new WeakHashMap<>());

    private TransactionLogRepository getRepo() {
        return SpringContext.getBean(TransactionLogRepository.class);
    }

    private ActivityLogRepository getActivityRepo() {
        return SpringContext.getBean(ActivityLogRepository.class);
    }

    @PostLoad
    public void postLoad(Object entity) {
        if (isAuditEntity(entity)) {
            return;
        }
        try {
            SNAPSHOTS.put(entity, mapper.writeValueAsString(entity));
        } catch (JsonProcessingException ex) {
            log.warn("Error serializing entity on postLoad for audit snapshot: {}", ex.getMessage());
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (isAuditEntity(entity)) {
            return;
        }
        try {
            String newValue = mapper.writeValueAsString(entity);
            saveAudit("INSERT", entity, null, newValue);
        } catch (JsonProcessingException e) {
            log.warn("Error serializing entity for audit on persist: {}", e.getMessage());
        } catch (Exception ex) {
            log.error("Error saving transaction log on persist", ex);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (isAuditEntity(entity)) {
            return;
        }
        try {
            String oldValue = SNAPSHOTS.get(entity);
            String newValue = mapper.writeValueAsString(entity);
            saveAudit("UPDATE", entity, oldValue, newValue);
            SNAPSHOTS.put(entity, newValue);
        } catch (JsonProcessingException e) {
            log.warn("Error serializing entity for audit on update: {}", e.getMessage());
        } catch (Exception ex) {
            log.error("Error saving transaction log on update", ex);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (isAuditEntity(entity)) {
            return;
        }
        try {
            String oldValue = mapper.writeValueAsString(entity);
            saveAudit("DELETE", entity, oldValue, null);
            SNAPSHOTS.remove(entity);
        } catch (JsonProcessingException e) {
            log.warn("Error serializing entity for audit on remove: {}", e.getMessage());
        } catch (Exception ex) {
            log.error("Error saving transaction log on remove", ex);
        }
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "system";
        }
        return authentication.getName();
    }

    private String getRequestIp() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
            String xForwardedFor = servletAttributes.getRequest().getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return servletAttributes.getRequest().getRemoteAddr();
        }
        return "unknown";
    }

    private boolean isAuditEntity(Object entity) {
        return entity instanceof TransactionLogJpaEntity || entity instanceof ActivityLogJpaEntity;
    }

    private void saveAudit(String action, Object entity, String oldValue, String newValue) {
        String user = getCurrentUser();
        String tableName = entity.getClass().getSimpleName();
        String ipAddress = getRequestIp();
        LocalDateTime now = LocalDateTime.now();

        TransactionLogJpaEntity txLog = TransactionLogJpaEntity.builder()
                .userId(user)
                .action(action)
                .tableName(tableName)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(now)
                .ipAddress(ipAddress)
                .build();
        getRepo().save(txLog);

        ActivityLogJpaEntity activityLog = ActivityLogJpaEntity.builder()
                .userId(user)
                .action(action)
                .tableName(tableName)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(now)
                .ipAddress(ipAddress)
                .build();
        getActivityRepo().save(activityLog);
    }
}



