package com.imgur.imgurservice.config;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * Configuration class for enabling virtual thread support in the application.
 * This class customizes the task executor and Tomcat's protocol handler to use virtual threads,
 * improving concurrency and scalability.
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * Configures an {@link AsyncTaskExecutor} that uses virtual threads.
     *
     * Virtual threads provide lightweight concurrency, allowing better resource utilization
     * compared to traditional threads. This configuration wraps the virtual thread executor
     * in a {@link TaskExecutorAdapter} for Spring's compatibility.
     *
     * @return an instance of {@link AsyncTaskExecutor} backed by virtual threads.
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Customizes Tomcat's protocol handler to use a virtual thread executor.
     *
     * This ensures that the embedded Tomcat server in Spring Boot also leverages virtual threads
     * for handling HTTP requests, resulting in improved concurrency without increasing thread overhead.
     *
     * @return a {@link TomcatProtocolHandlerCustomizer} to configure the protocol handler.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<ProtocolHandler> protocolHandler() {
        return p -> p.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}