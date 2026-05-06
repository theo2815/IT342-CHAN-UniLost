package edu.cit.chan.unilost.filter;

import edu.cit.chan.unilost.features.user.AccountStatus;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.features.auth.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String email = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                if (jwtUtils.validateToken(token)) {
                    email = jwtUtils.getEmailFromToken(token);
                }
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // H2 + H3: Verify user exists and is active, use DB role instead of token role
                Optional<UserEntity> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    // User deleted since token was issued
                    filterChain.doFilter(request, response);
                    return;
                }

                UserEntity user = userOpt.get();

                // H3: Check account status — suspended/deactivated users cannot access
                if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // H2: Use DB role, not token role
                String role = user.getRole().name();

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Log but don't block — let the request continue without authentication.
            // This prevents unhandled exceptions from producing responses without CORS headers.
            log.warn("JWT authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
