package his.infrastructure.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtFilter
 * Filtro personalizado que intercepta todas las peticiones HTTP.
 * Su función es:
 *  1️ Leer el encabezado "Authorization".
 *  2️ Extraer y validar el token JWT.
 *  3️ Si el token es válido, autentìca al usuario en el contexto de Spring Security.
 *  4️ Si no hay token o es inválido, simplemente deja pasar la petición (sin usuario autenticado).
 *
 * Extiende OncePerRequestFilter para garantizar que se ejecute UNA SOLA VEZ por petición,
 * evitando problemas en casos especiales como forwards o includes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_LENGTH = BEARER_PREFIX.length();

    /**
     * Método principal que se ejecuta en cada petición entrante (una sola vez por petición).
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,   // Petición HTTP entrante (headers, body, etc.)
            @NonNull HttpServletResponse response, // Respuesta HTTP saliente
            @NonNull FilterChain filterChain       // Cadena de filtros que procesan la petición
    ) throws ServletException, IOException {

        // Obtiene el encabezado Authorization (si existe)
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si no hay header o no empieza con "Bearer ", continúa sin autenticar
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Authorization header found or invalid format in request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Extrae el token quitando el prefijo "Bearer"
        jwt = authHeader.substring(BEARER_LENGTH);

        // Verifica si el token está en blacklist (revocado)
        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            log.warn("Intento de uso de token revocado (logout previo)");
            sendUnauthorizedResponse(response, "Token revocado. Por favor, inicia sesión nuevamente");
            return;
        }

        try {
            // Extrae el email (o username) desde el JWT
            userEmail = jwtService.getUserName(jwt);
            log.debug("JWT token extracted successfully");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token attempted");
            sendUnauthorizedResponse(response, "Token JWT expirado");
            return;
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            log.warn("Invalid JWT token format or unsupported");
            sendUnauthorizedResponse(response, "Token JWT inválido o mal formado");
            return;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
            sendUnauthorizedResponse(response, "Token JWT vacío o no válido");
            return;
        } catch (Exception e) {
            log.error("Unexpected error processing JWT token", e);
            sendUnauthorizedResponse(response, "Error al procesar el token JWT");
            return;
        }

        // Si el token tiene un email y aún no hay autenticación en el contexto actual...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Carga los detalles del usuario desde la base de datos
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Valida el token con el usuario (firma, expiración, coherencia)
                if (jwtService.validateToken(jwt, userDetails)) {
                    log.debug("JWT token validated successfully");

                    // Crea un objeto de autenticación para el contexto de Spring
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,          // Usuario autenticado
                            null,                 // Credenciales (no se guardan)
                            userDetails.getAuthorities() // Roles o permisos
                    );

                    // Agrega detalles de la petición actual (IP, headers, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Registra al usuario como autenticado en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("User successfully authenticated via JWT");
                } else {
                    log.warn("JWT token validation failed");
                }
            } catch (Exception e) {
                log.error("Error loading user details or validating token", e);
            }
        }

        // Continúa con el siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }

    /**
     * Envía una respuesta de error 401 con formato JSON.
     *
     * @param response La respuesta HTTP
     * @param message El mensaje de error
     * @throws IOException Si ocurre un error al escribir la respuesta
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": 401}", message));
    }
}
