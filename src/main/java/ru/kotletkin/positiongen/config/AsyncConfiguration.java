package ru.kotletkin.positiongen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "calculationExecutor")
    public Executor calculationExecutor(final CalculationProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.poolSize());
        executor.setMaxPoolSize(properties.poolSize());
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("fractalrabbit-");
        executor.initialize();
        return executor;
    }
}
