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
 * In-memory sliding window rate limiter.
 * - Auth endpoints: 10 requests/minute per IP
 * - Write endpoints (POST/PUT/DELETE): 30 requests/minute per IP
 * - Read endpoints (GET): 120 requests/minute per IP
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_MAX = 10;
    private static final int WRITE_MAX = 30;
    private static final int READ_MAX = 120;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip CORS preflight requests and WebSocket endpoints
        if ("OPTIONS".equalsIgnoreCase(method) || path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine rate limit tier
        int maxRequests;
        String tierKey;

        if (path.startsWith("/api/auth/")) {
            maxRequests = AUTH_MAX;
            tierKey = "auth";
        } else if ("GET".equals(method) || "OPTIONS".equals(method) || "HEAD".equals(method)) {
            maxRequests = READ_MAX;
            tierKey = "read";
        } else {
            maxRequests = WRITE_MAX;
            tierKey = "write";
        }

        String clientIp = getClientIp(request);
        String bucketKey = clientIp + ":" + tierKey;
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestCounts.computeIfAbsent(bucketKey, k -> new ConcurrentLinkedDeque<>());

        // Remove entries outside the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MS) {
            timestamps.pollFirst();
        }

        int remaining = maxRequests - timestamps.size();

        // Add rate limit headers (L4)
        response.setIntHeader("X-RateLimit-Limit", maxRequests);
        response.setIntHeader("X-RateLimit-Remaining", Math.max(remaining - 1, 0));
        response.setHeader("X-RateLimit-Reset", String.valueOf((now + WINDOW_MS) / 1000));

        if (timestamps.size() >= maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(WINDOW_MS / 1000));
            objectMapper.writeValue(response.getWriter(),
                    Map.of("error", "Too many requests. Please try again later."));
            return;
        }

        timestamps.addLast(now);
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
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
