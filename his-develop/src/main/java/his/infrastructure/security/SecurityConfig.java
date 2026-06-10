package his.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Configuración CORS (permitimos llamadas desde el frontend)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)

                // Rutas públicas y protegidas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/register/personal",
                                "/api/auth/register/admin",
                                "/api/auth/authenticate",
                                "/api/auth/logout",
                                "/api/catalogs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui-custom.html"
                        ).permitAll()
                        // Permitir a ADMIN y DOCTOR realizar consultas (GET) sobre la atencion de citas,
                        // pero solo DOCTOR puede ejecutar operaciones de mutacion (POST, PATCH, DELETE)
                        .requestMatchers(HttpMethod.GET, "/api/appointments/attention/**").hasAnyAuthority("ADMIN", "DOCTOR")
                        .requestMatchers("/api/appointments/attention/**").hasAuthority("DOCTOR")
                        .requestMatchers("/api/patients/register").hasAnyAuthority("ADMIN", "DOCTOR", "ENFERMERA", "LABORATORISTA", "FARMACEUTICO", "ADMINISTRATIVO", "RECEPCION")
                        .requestMatchers("/api/patients/triage").hasAnyAuthority("ADMIN", "DOCTOR", "ENFERMERA", "LABORATORISTA", "FARMACEUTICO", "ADMINISTRATIVO", "RECEPCION")
                        .requestMatchers("/api/patients/**").hasAnyAuthority("ADMIN", "DOCTOR", "ENFERMERA", "LABORATORISTA", "FARMACEUTICO", "ADMINISTRATIVO", "RECEPCION")
                        .requestMatchers("/api/laboratory/**").hasAnyAuthority("LABORATORISTA", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/pharmacy/prescriptions/**").hasAnyAuthority("FARMACEUTICO", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/pharmacy/dispense").hasAnyAuthority("FARMACEUTICO", "ADMIN")
                        .requestMatchers("/api/pharmacy/medicines").hasAnyAuthority("FARMACEUTICO", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/pharmacy/reminders/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Agregamos el filtro JWT antes del filtro de autenticación
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    // Configuración CORS global (puedes ajustar dominios según tu entorno)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Permitir frontend local y despliegues de Azure Static Web Apps.
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://localhost:5176",
                "http://localhost:5177",
                "http://localhost:5178",
                "http://127.0.0.1:5173",
                "https://*.azurestaticapps.net"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
