package his.infrastructure.security;

import his.domain.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jakarta.annotation.PostConstruct;

/**
 * Servicio centralizado para generación y validación de tokens JWT.
 *
 * Responsabilidades:
 * - Generar tokens JWT con claims personalizados (role, sub, idUser)
 * - Validar tokens (firma, expiración, usuario)
 * - Extraer información de claims
 * - Gestionar la clave de firma de forma segura
 *
 * <p><b>Configuración (application.properties):</b>
 * - jwt.secret.key: Clave secreta Base64 para firmar (debe tener >256 bits)
 * - jwt.expiration.hours: Horas de expiración del token (ej: 24)
 *
 * <p><b>Algoritmo:</b> HS256 (HMAC-SHA256)
 *
 * @see JwtFilter - Filtro que valida tokens en cada petición
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.hours}")
    private long tokenExpirationHours;

    // Clave de firma precalculada en la inicialización del bean (inyección de dependencias)
    private Key signingKey;

    /**
     * Inicializa la clave de firma al momento en que Spring crea el bean.
     * Se ejecuta automáticamente después de que se inyecten todas las dependencias.
     * Esto es más eficiente que double-checked locking en cada petición.
     */
    @PostConstruct
    public void initSigningKey() {
        try {
            byte[] decodedKey = Decoders.BASE64.decode(secretKey);
            this.signingKey = Keys.hmacShaKeyFor(decodedKey);
            logger.info("Clave de firma JWT inicializada correctamente en la creación del bean");
        } catch (IllegalArgumentException e) {
            logger.error("Error fatal: La clave JWT secreta es inválida o no está configurada", e);
            throw new IllegalStateException("La clave JWT secreta es inválida o no está configurada", e);
        }
    }

    /**
     * Genera un token JWT con los datos de un usuario_sistema (id, rol, email).
     * @param user Usuario autenticado.
     * @return Token JWT firmado.
     * @throws IllegalArgumentException Si el usuario es nulo
     */
    public String generateToken(User user){
        if (user == null) {
            logger.warn("Intento de generar token con usuario nulo");
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        logger.info("Generando token para usuario: {}", user.getEmail());

        try {
            Long idUser = user.getUserId();

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().name());
            claims.put("sub", user.getEmail());

            if (idUser != null) {
                claims.put("idUser", idUser);
            }

            String token = generateToken(claims, user.getEmail());
            logger.info("Token generado exitosamente para usuario: {}", user.getEmail());
            return token;
        } catch (Exception e) {
            logger.error("Error al generar token para usuario: {}", user.getEmail(), e);
            throw e;
        }
    }


    /**
     * Genera un token sin claims adicionales.
     */
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("Intento de generar token con UserDetails nulo");
            throw new IllegalArgumentException("UserDetails no puede ser nulo");
        }
        return generateToken(Map.of(), userDetails);
    }

    private String generateToken(Map<String, Object> extraClaims, String subject) {
        if (extraClaims == null) {
            extraClaims = new HashMap<>();
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("El subject del token no puede ser nulo o vacío");
        }
        Instant now = Instant.now();
        Instant expiration = now.plus(tokenExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * Genera un JWT con claims personalizados.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        if (extraClaims == null) {
            logger.debug("extraClaims es nulo, usando mapa vacío");
            extraClaims = new HashMap<>();
        }

        if (userDetails == null) {
            logger.warn("Intento de generar token con UserDetails nulo");
            throw new IllegalArgumentException("UserDetails no puede ser nulo");
        }

        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(tokenExpirationHours, ChronoUnit.HOURS);

            logger.debug("Creando token con expiración en {} horas", tokenExpirationHours);

            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error al generar token JWT para usuario: {}", userDetails.getUsername(), e);
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    /**
     * Obtiene el subject (email de usuario_sistema) desde el token.
     */
    public String getUserName(String token) {
        try {
            return getClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.debug("Error al extraer nombre de usuario del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae un claim específico del token usando una función lambda.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        if (token == null || token.isBlank()) {
            logger.warn("Intento de extraer claim de token nulo o vacío");
            throw new IllegalArgumentException("Token no puede ser nulo o vacío");
        }

        try {
            final Claims claims = getAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Error al extraer claim del token: {}", e.getMessage());
            throw new RuntimeException("Error al procesar el token", e);
        }
    }

    /**
     * Obtiene todos los claims de un token JWT.
     * @param token Token JWT a parsear
     * @return Claims del token
     * @throws io.jsonwebtoken.JwtException Si el token es inválido o está expirado
     */
    private Claims getAllClaims(String token){
        if (token == null || token.isBlank()) {
            logger.warn("Intento de parsear token nulo o vacío");
            throw new IllegalArgumentException("Token no puede ser nulo o vacío");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warn("Token JWT no soportado: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformado: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.warn("Firma JWT inválida: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Claims JWT vacíos: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al parsear token JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el token", e);
        }
    }

    /**
     * Obtiene la clave de firma previamente inicializada.
     * La clave se calcula una sola vez en @PostConstruct.
     *
     * @return Clave de firma para firmar/validar JWT
     * @throws IllegalStateException Si la clave no fue inicializada correctamente
     */
    private Key getSigningKey() {
        if (signingKey == null) {
            throw new IllegalStateException("La clave de firma JWT no fue inicializada correctamente. Verifica que la configuración 'jwt.secret.key' esté presente.");
        }
        return signingKey;
    }

    /**
     * Válida que un token pertenezca al usuario y que no esté expirado.
     */
    public boolean validateToken(String token, UserDetails userDetails){
        if (token == null || token.isBlank()) {
            logger.debug("Validación de token nulo o vacío");
            return false;
        }

        if (userDetails == null) {
            logger.warn("Validación con UserDetails nulo");
            return false;
        }

        try {
            final String username = getUserName(token);
            boolean isValid = (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);

            if (isValid) {
                logger.debug("Token válido para usuario: {}", username);
            } else {
                logger.warn("Token inválido o expirado para usuario: {}", userDetails.getUsername());
            }

            return isValid;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado para usuario: {}", userDetails.getUsername());
            return false;
        } catch (Exception e) {
            logger.warn("Error al validar token para usuario: {}: {}", userDetails.getUsername(), e.getMessage());
            return false;
        }
    }
    /**
     * Verifica si el token ha expirado.
     * @param token Token JWT a verificar
     * @return true si el token está expirado, false en caso contrario
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getAllClaims(token).getExpiration();

            // Validar que la fecha de expiración no sea nula
            if (expirationDate == null) {
                logger.warn("Token sin fecha de expiración definida");
                return true; // Considerar inválido si no tiene fecha de expiración
            }

            boolean expired = expirationDate.before(new Date());

            if (expired) {
                logger.debug("Token expirado. Fecha de expiración: {}", expirationDate);
            }

            return expired;
        } catch (ExpiredJwtException e) {
            logger.debug("Token JWT expirado (excepción detectada)");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar expiración del token: {}", e.getMessage());
            return true;
        }
    }

}
