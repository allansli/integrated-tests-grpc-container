package com.example.grpcclient.controller;

import com.example.grpcclient.model.GoodbyeRequest;
import com.example.grpcclient.model.HelloRequest;
import com.example.grpcclient.service.GreeterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greeter")
public class GreeterController {

    private final GreeterService greeterService;

    public GreeterController(GreeterService greeterService) {
        this.greeterService = greeterService;
    }

    @PostMapping("/hello")
    public ResponseEntity<String> sayHello(@RequestBody HelloRequest request) {
        String response = greeterService.sayHello(request.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/goodbye")
    public ResponseEntity<String> sayGoodbye(@RequestBody GoodbyeRequest request) {
        String response = greeterService.sayGoodbye(request.getName(), request.isFormal());
        return ResponseEntity.ok(response);
    }
}
