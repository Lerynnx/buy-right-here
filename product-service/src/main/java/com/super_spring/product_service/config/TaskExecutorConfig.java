package com.super_spring.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class TaskExecutorConfig {

    private final TaskDecorator mdcTaskDecorator = runnable -> {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            if (context != null) {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    };

    @Bean(name = ApplicationConstants.EXPORT_TASK_EXECUTOR)
    public Executor exportTaskExecutor(
            @Value("${app.task-executor.core-pool-size}") int corePoolSize,
            @Value("${app.task-executor.max-pool-size}") int maxPoolSize,
            @Value("${app.task-executor.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(ApplicationConstants.EXPORT_THREAD_PREFIX);
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean(name = ApplicationConstants.IMPORT_TASK_EXECUTOR)
    public Executor importTaskExecutor(
            @Value("${app.task-executor.core-pool-size}") int corePoolSize,
            @Value("${app.task-executor.max-pool-size}") int maxPoolSize,
            @Value("${app.task-executor.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(ApplicationConstants.IMPORT_THREAD_PREFIX);
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }
}