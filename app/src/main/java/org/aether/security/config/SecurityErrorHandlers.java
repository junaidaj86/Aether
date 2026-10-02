package org.aether.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aether.common.error.ApiError;
import org.aether.common.error.ErrorCode;
import org.aether.common.web.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;

@Configuration
public class SecurityErrorHandlers {

    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandlers.class);

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeAuthenticationError(
                objectMapper, request, response, exception);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeAccessDeniedError(
                objectMapper, request, response, exception);
    }

    private void writeAuthenticationError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        ErrorCode code = request.getHeader("Authorization") == null
                ? ErrorCode.AUTHENTICATION_REQUIRED
                : resolveAuthenticationCode(exception);

        log.warn("event=security_authentication_failed code={} path={}",
                code, request.getRequestURI());
        write(objectMapper, request, response, code, 401,
                code == ErrorCode.AUTHENTICATION_REQUIRED
                        ? "Authentication is required"
                        : "Authentication failed");
    }

    private void writeAccessDeniedError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException {

        log.warn("event=security_access_denied path={}", request.getRequestURI());
        write(objectMapper, request, response, ErrorCode.ACCESS_DENIED, 403,
                "Access is denied");
    }

    private ErrorCode resolveAuthenticationCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String code = oauth2Exception.getError().getErrorCode();
            if ("invalid_audience".equals(code)) return ErrorCode.INVALID_AUDIENCE;
            if ("untrusted_issuer".equals(code)) return ErrorCode.UNTRUSTED_ISSUER;
        }
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("issuer")) {
            return ErrorCode.UNTRUSTED_ISSUER;
        }
        return ErrorCode.INVALID_TOKEN;
    }

    private void write(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode code,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (status == 401) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        objectMapper.writeValue(response.getWriter(), new ApiError(
                code.name(),
                message,
                status,
                request.getRequestURI(),
                correlationId(request),
                Instant.now(),
                null));
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(CorrelationIdFilter.MDC_KEY);
        return value == null ? null : value.toString();
    }
}
