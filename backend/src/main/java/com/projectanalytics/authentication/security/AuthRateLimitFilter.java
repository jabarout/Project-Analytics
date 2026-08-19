package com.projectanalytics.authentication.security;

import com.projectanalytics.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple fixed-window rate limiter for public auth endpoints (Phase 5).
 * Keyed by client IP + route. Not a substitute for edge/WAF limits in production.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final SecurityErrorWriter securityErrorWriter;
    private final boolean enabled;
    private final int loginMax;
    private final int registerMax;
    private final int forgotMax;
    private final int resetMax;
    private final long windowSeconds;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            SecurityErrorWriter securityErrorWriter,
            @Value("${projectanalytics.security.rate-limit.enabled:true}") boolean enabled,
            @Value("${projectanalytics.security.rate-limit.login-max-attempts:10}") int loginMax,
            @Value("${projectanalytics.security.rate-limit.register-max-attempts:5}") int registerMax,
            @Value("${projectanalytics.security.rate-limit.forgot-password-max-attempts:5}") int forgotMax,
            @Value("${projectanalytics.security.rate-limit.reset-password-max-attempts:10}") int resetMax,
            @Value("${projectanalytics.security.rate-limit.window-seconds:300}") int windowSeconds
    ) {
        this.securityErrorWriter = securityErrorWriter;
        this.enabled = enabled;
        this.loginMax = loginMax;
        this.registerMax = registerMax;
        this.forgotMax = forgotMax;
        this.resetMax = resetMax;
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!enabled || !HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !(path.endsWith("/auth/login")
                || path.endsWith("/auth/register")
                || path.endsWith("/auth/forgot-password")
                || path.endsWith("/auth/reset-password")
                || path.endsWith("/auth/confirm-email")
                || path.endsWith("/auth/resend-confirmation"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = limitFor(path);
        String key = clientKey(request) + "|" + path;
        if (!allow(key, limit)) {
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            securityErrorWriter.write(response, path, ErrorCode.AUTH_007);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int limitFor(String path) {
        if (path.endsWith("/auth/login")) {
            return loginMax;
        }
        if (path.endsWith("/auth/register")) {
            return registerMax;
        }
        if (path.endsWith("/auth/forgot-password") || path.endsWith("/auth/resend-confirmation")) {
            return forgotMax;
        }
        // reset-password + confirm-email
        return resetMax;
    }

    private boolean allow(String key, int maxAttempts) {
        long now = Instant.now().getEpochSecond();
        long cutoff = now - windowSeconds;
        Deque<Long> queue = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
                queue.removeFirst();
            }
            if (queue.size() >= maxAttempts) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }
}
