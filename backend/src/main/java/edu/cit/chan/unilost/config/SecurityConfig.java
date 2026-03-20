package edu.cit.chan.unilost.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// TODO: [Phase 3] Add JWT refresh token rotation
// TODO: [Phase 6] Add WebSocket security configuration for real-time chat
// TODO: [Phase 8] Add rate limiting for auth endpoints
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            edu.cit.chan.unilost.filter.JwtAuthenticationFilter jwtAuthenticationFilter,
            edu.cit.chan.unilost.filter.RateLimitFilter rateLimitFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campuses", "/api/campuses/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Item endpoints — read is public, write requires authentication
                        .requestMatchers(HttpMethod.GET, "/api/items", "/api/items/**").permitAll()
                        .requestMatchers("/api/items/**").authenticated()
                        .requestMatchers("/api/items").authenticated()

                        // Claim endpoints — all require authentication
                        .requestMatchers("/api/claims/**").authenticated()
                        .requestMatchers("/api/claims").authenticated()

                        // TODO: [Phase 6] Add chat/message endpoints security rules
                        // TODO: [Phase 7] Add handover endpoints security rules
                        // TODO: [Phase 8] Add notification endpoints security rules

                        // Campus management (create/update/delete) requires ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/campuses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/campuses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/campuses/**").hasRole("ADMIN")

                        // User list (GET all) requires ADMIN or FACULTY
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN", "FACULTY")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter,
                        edu.cit.chan.unilost.filter.JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
