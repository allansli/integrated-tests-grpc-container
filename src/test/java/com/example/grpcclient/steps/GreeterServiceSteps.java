package com.example.grpcclient.steps;

import com.example.grpc.helloworld.GreeterGrpc;
import com.example.grpcclient.containers.GrpcMockServerContainer;
import com.example.grpcclient.model.GoodbyeRequest;
import com.example.grpcclient.service.GreeterService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(initializers = GreeterServiceSteps.Initializer.class)
public class GreeterServiceSteps {
    private static final Logger logger = LoggerFactory.getLogger(GreeterServiceSteps.class);
    private static final GrpcMockServerContainer grpcMockServer;
    
    // Initialize the container in a static block
    static {
        // Force system properties to be set before any Spring beans are created
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        
        grpcMockServer = new GrpcMockServerContainer("allansli/grpc-mock-server:latest");
        grpcMockServer.withLogConsumer(new Slf4jLogConsumer(logger));
        
        // Start the container
        logger.info("Starting gRPC mock server container...");
        grpcMockServer.start();
        logger.info("gRPC mock server container started at {}:{}", 
                grpcMockServer.getGrpcHost(), grpcMockServer.getGrpcPort());
                
        // Set system properties directly to ensure they're available to all components
        // These must be set before Spring context is created
        setSystemProperties();
    }
    
    /**
     * Helper method to set system properties consistently
     */
    private static void setSystemProperties() {
        // Clear any existing properties first
        System.clearProperty("grpc.server.host");
        System.clearProperty("grpc.server.port");
        System.clearProperty("mock.server.http.port");
        
        // Set the properties with the current container values
        System.setProperty("grpc.server.host", grpcMockServer.getGrpcHost());
        System.setProperty("grpc.server.port", String.valueOf(grpcMockServer.getGrpcPort()));
        System.setProperty("mock.server.http.port", String.valueOf(grpcMockServer.getHttpPort()));
        
        logger.info("Set system properties - grpc.server.host: {}, grpc.server.port: {}, mock.server.http.port: {}", 
                System.getProperty("grpc.server.host"), System.getProperty("grpc.server.port"), 
                System.getProperty("mock.server.http.port"));
    }
    
    // Custom initializer to set properties before Spring context is created
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // Make sure system properties are set
            setSystemProperties();
            
            // Set properties in Spring environment
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "grpc.server.host=" + grpcMockServer.getGrpcHost(),
                "grpc.server.port=" + grpcMockServer.getGrpcPort(),
                "mock.server.http.port=" + grpcMockServer.getHttpPort()
            );
            
            logger.info("Initializer set properties - grpc.server.host: {}, grpc.server.port: {}, mock.server.http.port: {}", 
                    grpcMockServer.getGrpcHost(), grpcMockServer.getGrpcPort(), grpcMockServer.getHttpPort());
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GreeterService greeterService;

    private ResponseEntity<String> response;
    private String baseUrl;
    private String mockServerUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Make sure system properties are set
        setSystemProperties();
        
        // Register dynamic properties
        registry.add("grpc.server.host", grpcMockServer::getGrpcHost);
        registry.add("grpc.server.port", grpcMockServer::getGrpcPort);
        registry.add("mock.server.http.port", grpcMockServer::getHttpPort);
        
        logger.info("DynamicPropertySource set properties - grpc.server.host: {}, grpc.server.port: {}, mock.server.http.port: {}", 
                grpcMockServer.getGrpcHost(), grpcMockServer.getGrpcPort(), grpcMockServer.getHttpPort());
    }

    @Before
    public void setup() {
        // Make sure system properties are set
        setSystemProperties();
        
        baseUrl = "http://localhost:" + port + "/api/greeter";
        mockServerUrl = "http://" + grpcMockServer.getHttpHost() + ":" + grpcMockServer.getHttpPort() + "/api";
        
        logger.info("Test REST endpoint URL: {}", baseUrl);
        logger.info("Mock server API URL: {}", mockServerUrl);
        logger.info("gRPC server connection: {}:{}", 
                grpcMockServer.getGrpcHost(), grpcMockServer.getGrpcPort());
        logger.info("Current system properties - grpc.server.host: {}, grpc.server.port: {}, mock.server.http.port: {}", 
                System.getProperty("grpc.server.host"), System.getProperty("grpc.server.port"), 
                System.getProperty("mock.server.http.port"));
        
        // Verify gRPC server is accessible
        verifyGrpcServerConnection();
        
        // Log info about the autowired greeterService
        logger.info("Autowired GreeterService: {}", greeterService);
    }
    
    private void verifyGrpcServerConnection() {
        logger.info("Verifying direct gRPC connection to server...");
        ManagedChannel channel = null;
        try {
            String host = System.getProperty("grpc.server.host");
            int port = Integer.parseInt(System.getProperty("grpc.server.port"));
            
            logger.info("Creating direct connection to {}:{}", host, port);
            
            channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
            
            GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
            
            // Create a simple request to test the connection
            com.example.grpc.helloworld.HelloRequest request = 
                com.example.grpc.helloworld.HelloRequest.newBuilder()
                    .setName("ConnectionTest")
                    .build();
            
            try {
                // Try to make a call - we don't care about the response, just that it doesn't throw
                stub.withDeadlineAfter(5, TimeUnit.SECONDS).sayHello(request);
                logger.info("Successfully connected to gRPC server directly");
            } catch (Exception e) {
                logger.error("Failed to connect to gRPC server: {}", e.getMessage());
                throw new RuntimeException("gRPC server is not accessible", e);
            }
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Given("the gRPC server is running")
    public void theGrpcServerIsRunning() {
        logger.info("Checking if gRPC server is running...");
        assertThat(grpcMockServer.isRunning()).isTrue();
        logger.info("gRPC server is running at {}:{}", 
                grpcMockServer.getGrpcHost(), grpcMockServer.getGrpcPort());
                
        // Double check system properties are set correctly
        assertThat(System.getProperty("grpc.server.host")).isEqualTo(grpcMockServer.getGrpcHost());
        assertThat(System.getProperty("grpc.server.port")).isEqualTo(String.valueOf(grpcMockServer.getGrpcPort()));
    }

    @Given("the HTTP server is running")
    public void theHttpServerIsRunning() {
        logger.info("Checking if HTTP server is running...");
        assertThat(grpcMockServer.isRunning()).isTrue();
        logger.info("HTTP server is running at {}:{}", 
                grpcMockServer.getHttpHost(), grpcMockServer.getHttpPort());
    }

    @When("I send a hello request with name {string}")
    public void iSendAHelloRequestWithName(String name) {
        com.example.grpcclient.model.HelloRequest request = new com.example.grpcclient.model.HelloRequest();
        request.setName(name);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<com.example.grpcclient.model.HelloRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending hello request with name: {}", name);
        response = restTemplate.postForEntity(baseUrl + "/hello", entity, String.class);
        logger.info("Received response: {}", response.getBody());
    }
    
    @When("I send a hello request with name {string} again")
    public void iSendAHelloRequestWithNameAgain(String name) {
        // Reuse the existing method
        iSendAHelloRequestWithName(name);
    }

    @Then("I should receive a hello response containing {string}")
    public void iShouldReceiveAHelloResponseContaining(String name) {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains(name);
    }

    @When("I send a formal goodbye request with name {string}")
    public void iSendAFormalGoodbyeRequestWithName(String name) {
        GoodbyeRequest request = new GoodbyeRequest();
        request.setName(name);
        request.setFormal(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GoodbyeRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending formal goodbye request with name: {}", name);
        response = restTemplate.postForEntity(baseUrl + "/goodbye", entity, String.class);
        logger.info("Received response: {}", response.getBody());
    }

    @Then("I should receive a formal goodbye response")
    public void iShouldReceiveAFormalGoodbyeResponse() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotEmpty();
    }

    @When("I send an informal goodbye request with name {string}")
    public void iSendAnInformalGoodbyeRequestWithName(String name) {
        GoodbyeRequest request = new GoodbyeRequest();
        request.setName(name);
        request.setFormal(false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GoodbyeRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Sending informal goodbye request with name: {}", name);
        response = restTemplate.postForEntity(baseUrl + "/goodbye", entity, String.class);
        logger.info("Received response: {}", response.getBody());
    }

    @Then("I should receive an informal goodbye response")
    public void iShouldReceiveAnInformalGoodbyeResponse() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotEmpty();
    }
    
    @When("I set an override response for the {string} method with message {string}")
    public void iSetAnOverrideResponseForTheMethodWithMessage(String methodName, String message) {
        Map<String, Object> overrideRequest = new HashMap<>();
        overrideRequest.put("serviceName", "helloworld.Greeter");
        overrideRequest.put("methodName", methodName);
        
        Map<String, String> responsePayload = new HashMap<>();
        responsePayload.put("message", message);
        overrideRequest.put("responsePayload", responsePayload);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(overrideRequest, headers);
        
        logger.info("Setting override response for method '{}' with message: {}", methodName, message);
        logger.info("Using application endpoint: {}/override", baseUrl.replace("/greeter", ""));
        
        // Use our application's override endpoint
        ResponseEntity<String> overrideResponse = restTemplate.postForEntity(
            mockServerUrl + "/override", entity, String.class);
        
        assertThat(overrideResponse.getStatusCode().is2xxSuccessful()).isTrue();
        logger.info("Override response set successfully: {}", overrideResponse.getBody());
    }
}
