package edu.cit.chan.unilost.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory sliding window rate limiter for authentication endpoints.
 * Limits to 10 requests per minute per IP address on /api/auth/** paths.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestCounts.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());

        // Remove entries outside the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("error", "Too many requests. Please try again later."));
            return;
        }

        timestamps.addLast(now);
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        // Only use remoteAddr to prevent X-Forwarded-For spoofing.
        // If running behind a trusted reverse proxy, configure
        // server.forward-headers-strategy=NATIVE in application.properties instead.
        return request.getRemoteAddr();
    }

    /** Cleanup stale entries every 5 minutes */
    @Scheduled(fixedRate = 300_000)
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        requestCounts.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            timestamps.removeIf(ts -> ts < cutoff);
            return timestamps.isEmpty();
        });
    }
}
