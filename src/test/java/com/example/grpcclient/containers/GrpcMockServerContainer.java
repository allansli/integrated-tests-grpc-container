package com.example.grpcclient.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.time.Duration;

public class GrpcMockServerContainer extends GenericContainer<GrpcMockServerContainer> {
    private static final Logger logger = LoggerFactory.getLogger(GrpcMockServerContainer.class);
    private static final int GRPC_PORT = 9090;
    private static final int HTTP_PORT = 8080;
    
    public GrpcMockServerContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));
        
        // Configure container
        withExposedPorts(GRPC_PORT, HTTP_PORT);
        withEnv("GRPC_SERVER_PORT", String.valueOf(GRPC_PORT));
        withLogConsumer(new Slf4jLogConsumer(logger));
        
        
        // Mount directories using bind mounts
        File protosDir = new File("./src/test/resources/protos");
        File responsesFile = new File("./src/test/resources/responses/responses.json");
        
        
        if (!protosDir.exists()) {
            throw new IllegalStateException("Protos directory not found: " + protosDir.getAbsolutePath());
        }
        
        if (!responsesFile.exists()) {
            throw new IllegalStateException("Responses file not found: " + responsesFile.getAbsolutePath());
        }
        
        // Use non-deprecated methods for mounting files
        withCopyFileToContainer(
            MountableFile.forHostPath(protosDir.getAbsolutePath()),
            "/app/app/protos"
        );
        withCopyFileToContainer(
            MountableFile.forHostPath(responsesFile.getAbsolutePath()),
            "/app/app/config/responses.json"
        );
        
        // Wait for the container to be ready
        waitingFor(Wait.forLogMessage(".*server running.*\\n", 1)
                .withStartupTimeout(Duration.ofSeconds(25)));
        
    }

    public String getGrpcHost() {
        return getHost();
    }

    public Integer getGrpcPort() {
        return getMappedPort(GRPC_PORT);
    }
    
    public String getHttpHost() {
        return getHost();
    }

    public Integer getHttpPort() {
        return getMappedPort(HTTP_PORT);
    }

    @Override
    public void start() {
        super.start();
        
        logger.info("Container started. gRPC server available at {}:{}", getGrpcHost(), getGrpcPort());
        logger.info("HTTP server available at {}:{}", getHttpHost(), getHttpPort());
    }
}
