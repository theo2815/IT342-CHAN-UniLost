package edu.cit.chan.unilost.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            edu.cit.chan.unilost.filter.JwtAuthenticationFilter jwtAuthenticationFilter,
            edu.cit.chan.unilost.filter.RateLimitFilter rateLimitFilter) throws Exception {
        http
                // CSRF is disabled because this is a stateless JWT-based API with no cookie-based sessions.
                // If cookie-based auth is ever added, CSRF must be re-enabled (especially for SockJS fallback).
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' https://res.cloudinary.com data:; connect-src 'self' ws: wss:; frame-ancestors 'none'"
                        ))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campuses", "/api/campuses/**").permitAll()

                        // Admin endpoints (ADMIN + FACULTY can access)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "FACULTY")

                        // Item endpoints — read is public, write requires authentication
                        .requestMatchers(HttpMethod.GET, "/api/items", "/api/items/**").permitAll()
                        .requestMatchers("/api/items/**").authenticated()
                        .requestMatchers("/api/items").authenticated()

                        // Claim endpoints — all require authentication
                        .requestMatchers("/api/claims/**").authenticated()
                        .requestMatchers("/api/claims").authenticated()

                        // Chat endpoints — all require authentication
                        .requestMatchers("/api/chats/**").authenticated()
                        .requestMatchers("/api/chats").authenticated()

                        // WebSocket endpoint — permitAll (auth handled via STOMP)
                        .requestMatchers("/ws/**").permitAll()

                        // Campus management (create/update/delete) requires ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/campuses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/campuses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/campuses/**").hasRole("ADMIN")

                        // Public leaderboard
                        .requestMatchers(HttpMethod.GET, "/api/users/leaderboard").permitAll()

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

    /**
     * Top-level servlet CorsFilter registered at HIGHEST_PRECEDENCE.
     * This guarantees CORS headers are on every response even if
     * Spring Security or any downstream filter throws an exception.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset", "Retry-After"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
