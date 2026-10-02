package org.aether.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final int MAX_LENGTH = 128;

    public static final String HEADER_NAME = RequestContext.CORRELATION_ID_HEADER;
    public static final String MDC_KEY = RequestContext.CORRELATION_ID;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(HEADER_NAME);
        String traceId = request.getHeader(RequestContext.TRACE_ID_HEADER);

        if (!isValid(correlationId)) {
            if (correlationId != null && !correlationId.isBlank()) {
                log.warn("event=invalid_correlation_id action=replace_request_id");
            }
            correlationId = UUID.randomUUID().toString();
        }

        if (!isValid(traceId)) {
            traceId = UUID.randomUUID().toString();
        }

        long startedAt = System.nanoTime();
        var previousContext = MDC.getCopyOfContextMap();

        try {
            MDC.remove(RequestContext.EXECUTION_ID);
            MDC.remove(RequestContext.ATTEMPT);
            MDC.put(RequestContext.TRACE_ID, traceId);
            MDC.put(MDC_KEY, correlationId);

            request.setAttribute(RequestContext.TRACE_ID, traceId);
            request.setAttribute(MDC_KEY, correlationId);

            response.setHeader(RequestContext.TRACE_ID_HEADER, traceId);
            response.setHeader(
                    HEADER_NAME,
                    correlationId
            );

            filterChain.doFilter(request, response);

        } finally {
            log.info("event=http_request_completed method={} path={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            restoreContext(previousContext);
        }
    }

    private void restoreContext(java.util.Map<String, String> previousContext) {
        MDC.clear();
        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        }
    }

    private boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.length() <= MAX_LENGTH
                && value.matches("[A-Za-z0-9._:-]+");
    }
}
