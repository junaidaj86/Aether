package org.aether.common.web;

import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * Names and lifecycle helpers for identifiers that follow a gateway operation.
 *
 * <p>Request context is carried by MDC for logging and by request attributes
 * for code handling the current servlet request. Async boundaries must use a
 * context-propagating executor.</p>
 */
public final class RequestContext {

    public static final String TRACE_ID = "traceId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String EXECUTION_ID = "executionId";
    public static final String ATTEMPT = "attempt";

    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String EXECUTION_ID_HEADER = "X-Execution-ID";

    private RequestContext() {
    }

    public static String current(String key) {
        return MDC.get(key);
    }

    /**
     * Starts one asynchronous execution and restores the previous MDC values
     * when the execution scope closes.
     */
    public static ExecutionScope startExecution() {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        MDC.put(EXECUTION_ID, UUID.randomUUID().toString());
        MDC.put(ATTEMPT, "1");
        return () -> restore(previous);
    }

    public static void setAttempt(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be greater than zero");
        }
        MDC.put(ATTEMPT, Integer.toString(attempt));
    }

    private static void restore(Map<String, String> previous) {
        MDC.clear();
        if (previous != null) {
            MDC.setContextMap(previous);
        }
    }

    @FunctionalInterface
    public interface ExecutionScope extends AutoCloseable {
        @Override
        void close();
    }
}
