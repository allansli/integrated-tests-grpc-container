Feature: Greeter Service Integration Tests
  As a user of the Greeter API
  I want to be able to send hello and goodbye requests
  So that I can receive appropriate greetings from the gRPC service

  Scenario: Say hello to a user
    Given the gRPC server is running
    When I send a hello request with name "John"
    Then I should receive a hello response containing "John"

  Scenario: Say formal goodbye to a user
    Given the gRPC server is running
    When I send a formal goodbye request with name "John"
    Then I should receive a formal goodbye response

  Scenario: Say informal goodbye to a user
    Given the gRPC server is running
    When I send an informal goodbye request with name "John"
    Then I should receive an informal goodbye response

  Scenario: Override hello response for next call
    Given the gRPC server is running
    When I set an override response for the "sayHello" method with message "This is an overridden response!"
    And I send a hello request with name "John"
    Then I should receive a hello response containing "This is an overridden response!"
    When I send a hello request with name "John" again
    Then I should receive a hello response containing "John"
