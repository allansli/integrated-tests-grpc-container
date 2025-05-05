package com.example.grpcclient.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for the hello endpoint")
public class HelloRequest {
    @Schema(description = "Name of the person to greet", example = "John", required = true)
    private String name;

    public HelloRequest() {
    }

    public HelloRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
