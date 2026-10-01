package org.aether.common.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "gatewayTaskExecutor")
    public ThreadPoolTaskExecutor gatewayTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("gateway-");
        executor.setTaskDecorator(contextPropagatingTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskDecorator contextPropagatingTaskDecorator() {
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(new Slf4jThreadLocalAccessor());
        return new ContextPropagatingTaskDecorator();
    }
}
