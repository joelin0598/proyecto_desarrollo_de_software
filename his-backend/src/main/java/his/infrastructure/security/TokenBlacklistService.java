package his.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de blacklist de tokens JWT.
 *
 * Mantiene en memoria un conjunto de tokens revocados (logouts).
 * Cuando un usuario hace logout, su token se agrega aquí.
 * El JwtFilter valida que el token no esté en blacklist antes de autenticar.
 *
 * <p><b>NOTA:</b> Esta implementación usa memoria (HashMap). Para producción con múltiples instancias,
 * se recomienda usar Redis distribuido.
 *
 * @see JwtFilter#doFilterInternal() - Valida blacklist
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    /**
     * Conjunto thread-safe de tokens revocados.
     * Estructura: Set<jwtToken> de tokens inválidos
     */
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Agrega un token a la blacklist (revocación).
     * Se llama cuando el usuario hace logout.
     *
     * @param token Token JWT a revocar
     */
    public void addToBlacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklistedTokens.add(token);
            logger.info("Token agregado a blacklist");
        }
    }

    /**
     * Verifica si un token está en blacklist (revocado).
     *
     * @param token Token JWT a verificar
     * @return true si el token está revocado, false si es válido
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return blacklistedTokens.contains(token);
    }

    /**
     * Obtiene el tamaño actual de la blacklist.
     * Útil para monitoreo.
     *
     * @return Cantidad de tokens revocados
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Limpia toda la blacklist.
     * CUIDADO: Solo usar en testing o mantenimiento.
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
        logger.warn("Blacklist de tokens vaciada");
    }
}

