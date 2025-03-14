# Spring Boot gRPC Client Application with Integration Testing

This project demonstrates a Spring Boot application that communicates with a gRPC server, along with comprehensive integration testing using Cucumber, Testcontainers, and a mock gRPC server.

## Project Structure

### Main Application
- `src/main/proto/helloworld.proto`: Proto definition file for the gRPC service
- `src/main/java/com/example/grpcclient`: Main application code
  - `GrpcClientApplication.java`: Spring Boot application entry point
  - `config/GrpcClientConfig.java`: Configuration for gRPC client with dynamic property support
  - `service/GreeterService.java`: Service to handle gRPC client operations
  - `controller/GreeterController.java`: REST controller exposing HTTP endpoints
  - `controller/OverrideController.java`: Controller for handling mock response overrides
  - `model/`: Data models for REST API requests and responses

### Integration Tests
- `src/test/java/com/example/grpcclient`: Test code
  - `CucumberIntegrationTest.java`: Cucumber test runner
  - `CucumberTestConfig.java`: Spring configuration for Cucumber tests
  - `containers/GrpcMockServerContainer.java`: Testcontainers setup for the mock gRPC server
  - `steps/GreeterServiceSteps.java`: Cucumber step definitions
- `src/test/resources/features`: Cucumber feature files
  - `greeter_service.feature`: BDD scenarios for testing the gRPC client
- `src/test/resources/responses`: Mock response configurations
  - `responses.json`: Predefined responses for the mock gRPC server
- `src/test/resources/protos`: Proto files for the mock server

## Features

### REST API Endpoints

The application exposes the following REST endpoints:

1. **Say Hello**
   - URL: `POST /api/greeter/hello`
   - Request Body: `{ "name": "YourName" }`
   - Response: The greeting message from the gRPC server

2. **Say Goodbye**
   - URL: `POST /api/greeter/goodbye`
   - Request Body: `{ "name": "YourName", "formal": true }`
   - Response: The goodbye message from the gRPC server

### Dynamic Configuration

The application supports dynamic configuration of the gRPC server connection:

- In production: Uses the configured values from `application.properties`
- In tests: Uses dynamic properties from the Testcontainers mock server

### Mock Server Integration

The integration tests use a Docker-based mock gRPC server with the following features:

- Predefined responses based on request content
- Dynamic response overrides via HTTP API
- File system bindings for proto definitions and response configurations

## Configuration

### Application Properties

```properties
# Server configuration
server.port=8081

# gRPC server configuration (default values)
grpc.server.host=localhost
grpc.server.port=9090

# Logging
logging.level.com.example.grpcclient=INFO
```

### Test Properties

```properties
# Test configuration
spring.main.allow-bean-definition-overriding=true
spring.main.lazy-initialization=true

# Logging for tests
logging.level.com.example.grpcclient=DEBUG

# Default gRPC server settings (will be overridden by dynamic properties)
grpc.server.host=localhost
grpc.server.port=9090
```

## Building and Running

### Running the Application

```bash
mvn spring-boot:run
```

### Running the Tests

```bash
mvn test
```

## Testing the API

You can test the API using curl or any REST client:

```bash
# Say Hello
curl -X POST http://localhost:8081/api/greeter/hello -H "Content-Type: application/json" -d "{\"name\":\"John\"}"

# Say Goodbye
curl -X POST http://localhost:8081/api/greeter/goodbye -H "Content-Type: application/json" -d "{\"name\":\"John\",\"formal\":true}"

# Set Override Response
curl -X POST http://localhost:8081/api/override -H "Content-Type: application/json" -d "{\"serviceName\":\"helloworld.Greeter\",\"methodName\":\"sayHello\",\"responsePayload\":{\"message\":\"Custom response\"}}"
```

Note: When using Windows PowerShell, ensure JSON payloads are properly escaped as shown above.

## Key Implementation Details

1. **Dynamic Property Handling**: The application uses a combination of system properties and Spring's `@DynamicPropertySource` to ensure the gRPC client uses the correct server details during tests.

2. **Lazy Initialization**: The gRPC client beans are lazily initialized to ensure they pick up the latest configuration values.

3. **Testcontainers Integration**: The mock gRPC server runs in a Docker container managed by Testcontainers, with proper lifecycle management.

4. **Override Mechanism**: The application includes a mechanism to override gRPC responses for testing purposes, allowing for flexible test scenarios.
