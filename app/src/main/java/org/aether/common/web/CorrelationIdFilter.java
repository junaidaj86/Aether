package org.aether.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final int MAX_LENGTH = 128;

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(HEADER_NAME);

        if (!isValid(correlationId)) {
            if (correlationId != null && !correlationId.isBlank()) {
                log.warn("event=invalid_correlation_id action=replace_request_id");
            }
            correlationId = UUID.randomUUID().toString();
        }

        long startedAt = System.nanoTime();

        try {
            MDC.put(MDC_KEY, correlationId);

            request.setAttribute(MDC_KEY, correlationId);

            response.setHeader(
                    HEADER_NAME,
                    correlationId
            );

            filterChain.doFilter(request, response);

        } finally {
            log.info("event=http_request_completed method={} path={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            MDC.remove(MDC_KEY);
        }
    }

    private boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.length() <= MAX_LENGTH
                && value.matches("[A-Za-z0-9._:-]+");
    }
}
