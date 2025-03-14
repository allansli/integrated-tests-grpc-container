package com.example.grpcclient.model;

public class GoodbyeRequest {
    private String name;
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
