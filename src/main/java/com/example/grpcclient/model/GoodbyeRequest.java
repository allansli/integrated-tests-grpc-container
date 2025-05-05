package com.example.grpcclient.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for the goodbye endpoint")
public class GoodbyeRequest {
    @Schema(description = "Name of the person to say goodbye to", example = "John", required = true)
    private String name;
    
    @Schema(description = "Whether to use formal language in the goodbye message", example = "true", defaultValue = "false")
    private boolean formal;

    public GoodbyeRequest() {
    }

    public GoodbyeRequest(String name, boolean formal) {
        this.name = name;
        this.formal = formal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFormal() {
        return formal;
    }

    public void setFormal(boolean formal) {
        this.formal = formal;
    }
}
