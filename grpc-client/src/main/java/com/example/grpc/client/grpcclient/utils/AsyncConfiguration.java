package com.example.grpc.client.grpcclient.utils;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration
{
    /**
     * Creating Task pool executor to perform asynchronous function calls
     * Reference:   https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/scheduling.html,
     *              https://medium.com/trendyol-tech/spring-boot-async-executor-management-with-threadpooltaskexecutor-f493903617d
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(25);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsynchThread-");
        executor.initialize();
        return executor;
    }
}