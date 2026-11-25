package com.clearcareai.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.clearcareai.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Protects the two endpoints that only the FastAPI service should call:
//   GET /api/reviews/by-call-id/{callId}
//   PUT /api/reviews/{id}/process
// FastAPI has no JWT, so these stay permitAll in SecurityConfig.
// This filter is the lock: the caller must send the shared secret in a header.
@Component
@Slf4j
@RequiredArgsConstructor
public class InternalSecretFilter extends OncePerRequestFilter {

    public static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final InternalSecretProperties internalSecretProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // local dev and tests: the check is switched off
        if (!Boolean.TRUE.equals(internalSecretProperties.getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }

        // every other endpoint passes through untouched
        if (!isInternalEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String sentSecret = request.getHeader(INTERNAL_SECRET_HEADER);
        String expectedSecret = internalSecretProperties.getValue();

        // enabled but no secret configured: reject, never let everyone in
        if (!StringUtils.hasText(sentSecret) || !StringUtils.hasText(expectedSecret)) {
            reject(request, response);
            return;
        }

        // constant-time comparison: takes the same time whether the first or the
        // last character is wrong, so the secret cannot be guessed from timing
        boolean matches = MessageDigest.isEqual(
                sentSecret.getBytes(StandardCharsets.UTF_8),
                expectedSecret.getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            reject(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isInternalEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/api/reviews/by-call-id/")) {
            return true;
        }

        boolean isPut = "PUT".equalsIgnoreCase(request.getMethod());
        return isPut && path.matches("/api/reviews/\\d+/process");
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("Rejected internal call without a valid secret: {} {}", request.getMethod(), request.getRequestURI());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error("Unauthorized: missing or invalid internal secret")));
    }
}
