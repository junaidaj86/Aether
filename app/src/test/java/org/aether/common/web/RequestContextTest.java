package org.aether.common.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RequestContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void startsExecutionWithIdAndFirstAttempt() {
        MDC.put(RequestContext.CORRELATION_ID, "request-123");

        try (RequestContext.ExecutionScope ignored = RequestContext.startExecution()) {
            assertThat(RequestContext.current(RequestContext.CORRELATION_ID))
                    .isEqualTo("request-123");
            assertThat(RequestContext.current(RequestContext.EXECUTION_ID))
                    .isNotBlank();
            assertThat(RequestContext.current(RequestContext.ATTEMPT))
                    .isEqualTo("1");
        }

        assertThat(RequestContext.current(RequestContext.CORRELATION_ID))
                .isEqualTo("request-123");
        assertThat(RequestContext.current(RequestContext.EXECUTION_ID))
                .isNull();
    }

    @Test
    void updatesAttemptAndRejectsInvalidValues() {
        RequestContext.setAttempt(2);

        assertThat(RequestContext.current(RequestContext.ATTEMPT)).isEqualTo("2");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> RequestContext.setAttempt(0));
    }
}
