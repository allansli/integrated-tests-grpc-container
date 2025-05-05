package com.example.grpcclient.config;

import com.example.grpc.helloworld.GreeterGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(GrpcClientConfig.class);

    // Default values that will be used if system properties are not set
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9090;

    private ManagedChannel channel;

    /**
     * Always gets the latest host from system properties if available
     */
    private String getEffectiveHost() {
        String host = System.getProperty("grpc.server.host");
        if (host != null && !host.isEmpty()) {
            logger.info("Using system property for gRPC host: {}", host);
            return host;
        }
        logger.info("Using default gRPC host: {}", DEFAULT_HOST);
        return DEFAULT_HOST;
    }

    /**
     * Always gets the latest port from system properties if available
     */
    private int getEffectivePort() {
        String port = System.getProperty("grpc.server.port");
        if (port != null && !port.isEmpty()) {
            try {
                int portNum = Integer.parseInt(port);
                logger.info("Using system property for gRPC port: {}", portNum);
                return portNum;
            } catch (NumberFormatException e) {
                logger.error("Invalid port number in system property: {}", port);
            }
        }
        logger.info("Using default gRPC port: {}", DEFAULT_PORT);
        return DEFAULT_PORT;
    }

    @Bean
    @Lazy
    public ManagedChannel managedChannel() {
        // Log all system properties to debug
        logger.info("Creating gRPC channel - checking system properties:");
        System.getProperties().forEach((key, value) -> {
            if (key.toString().contains("grpc")) {
                logger.info("System property: {} = {}", key, value);
            }
        });

        // Always use the latest host and port from system properties
        String effectiveHost = getEffectiveHost();
        int effectivePort = getEffectivePort();

        logger.info("Creating gRPC channel to {}:{}", effectiveHost, effectivePort);

        // Close any existing channel before creating a new one
        if (channel != null) {
            try {
                logger.info("Shutting down existing channel");
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Error shutting down channel", e);
            }
        }

        // Use virtual threads for gRPC calls
        channel = ManagedChannelBuilder.forAddress(effectiveHost, effectivePort)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .usePlaintext()
                .build();
        logger.info("Successfully created gRPC channel to {}:{}", effectiveHost, effectivePort);
        return channel;
    }

    @Bean
    @Lazy
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub(ManagedChannel channel) {
        logger.info("Creating gRPC blocking stub for channel: {}", channel);
        return GreeterGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down gRPC channel");
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
