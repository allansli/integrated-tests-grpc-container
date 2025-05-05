package com.example.grpcclient.controller;

import com.example.grpcclient.model.GoodbyeRequest;
import com.example.grpcclient.model.HelloRequest;
import com.example.grpcclient.service.GreeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greeter")
@Tag(name = "Greeter API", description = "REST endpoints for the Greeter service backed by gRPC")
public class GreeterController {

    private final GreeterService greeterService;

    public GreeterController(GreeterService greeterService) {
        this.greeterService = greeterService;
    }

    @Operation(summary = "Send a hello request", 
               description = "Sends a hello request to the gRPC server and returns a greeting response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hello request processed successfully",
                     content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, 
                                       schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                     content = @Content)
    })
    @PostMapping("/hello")
    public ResponseEntity<String> sayHello(
        @Parameter(description = "Hello request containing the name to greet", required = true)
        @RequestBody HelloRequest request) {
        String response = greeterService.sayHello(request.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send a goodbye request", 
               description = "Sends a goodbye request to the gRPC server and returns a farewell response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goodbye request processed successfully",
                     content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, 
                                       schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                     content = @Content)
    })
    @PostMapping("/goodbye")
    public ResponseEntity<String> sayGoodbye(
        @Parameter(description = "Goodbye request containing the name and formality flag", required = true)
        @RequestBody GoodbyeRequest request) {
        String response = greeterService.sayGoodbye(request.getName(), request.isFormal());
        return ResponseEntity.ok(response);
    }
}
