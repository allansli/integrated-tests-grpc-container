# Integration Tests with Cucumber, Gherkin, and Testcontainers

This directory contains integration tests for the Spring Boot gRPC client application using Cucumber, Gherkin, and Testcontainers.

## Overview

The integration tests verify that the Spring Boot application correctly communicates with the gRPC mock server by:

1. Starting a Docker container with your gRPC mock server using Testcontainers
2. Configuring the Spring Boot application to connect to this container
3. Sending HTTP requests to the Spring Boot application
4. Verifying that the responses from the gRPC server are correctly processed

## Test Structure

- `features/greeter_service.feature`: Gherkin feature file defining test scenarios
- `steps/GreeterServiceSteps.java`: Step definitions implementing the Gherkin scenarios
- `containers/GrpcMockServerContainer.java`: Custom Testcontainer for the gRPC mock server
- `CucumberTestConfig.java`: Configuration for Cucumber and Spring integration
- `CucumberIntegrationTest.java`: Test runner for Cucumber tests

## Running the Tests

Before running the tests, make sure:

1. Docker is installed and running on your machine
2. Update the Docker image name in `GreeterServiceSteps.java` to match your actual gRPC mock server image

To run the tests:

```bash
mvn test
```

## Test Scenarios

The tests cover the following scenarios:

1. Sending a hello request and verifying the response
2. Sending a formal goodbye request and verifying the response
3. Sending an informal goodbye request and verifying the response

## Customizing the Tests

You can customize the tests by:

1. Adding more scenarios to the feature file
2. Enhancing the assertions in the step definitions
3. Configuring the gRPC mock server container with additional settings
