package org.aether.common.config;

import org.aether.common.web.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();
    private final ThreadPoolTaskExecutor executor = asyncConfig.gatewayTaskExecutor();

    @AfterEach
    void shutdown() {
        MDC.clear();
        executor.shutdown();
    }

    @Test
    void propagatesGatewayIdentifiersToWorkerThread() throws Exception {
        MDC.put(RequestContext.TRACE_ID, "trace-123");
        MDC.put(RequestContext.CORRELATION_ID, "request-123");

        try (RequestContext.ExecutionScope ignored = RequestContext.startExecution()) {
            Future<String> execution = executor.submit(() -> String.join("/",
                    MDC.get(RequestContext.TRACE_ID),
                    MDC.get(RequestContext.CORRELATION_ID),
                    MDC.get(RequestContext.EXECUTION_ID),
                    MDC.get(RequestContext.ATTEMPT)));

            assertThat(execution.get())
                    .matches("trace-123/request-123/[0-9a-f-]+/1");
        }
    }

    @Test
    void usesContextPropagatingDecorator() {
        TaskDecorator decorator = asyncConfig.contextPropagatingTaskDecorator();

        assertThat(decorator).isInstanceOf(
                org.springframework.core.task.support.ContextPropagatingTaskDecorator.class);
    }
}
