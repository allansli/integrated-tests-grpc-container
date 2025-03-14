package com.example.grpcclient.service;

import com.example.grpc.helloworld.GoodbyeReply;
import com.example.grpc.helloworld.GoodbyeRequest;
import com.example.grpc.helloworld.GreeterGrpc;
import com.example.grpc.helloworld.HelloReply;
import com.example.grpc.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GreeterService {
    private static final Logger logger = LoggerFactory.getLogger(GreeterService.class);

    private final GreeterGrpc.GreeterBlockingStub greeterStub;

    public GreeterService(GreeterGrpc.GreeterBlockingStub greeterStub) {
        this.greeterStub = greeterStub;
        logger.info("GreeterService initialized with stub: {}", greeterStub);
    }

    public String sayHello(String name) {
        logger.debug("Sending hello request with name: {}", name);
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        
        try {
            HelloReply response = greeterStub.sayHello(request);
            logger.debug("Received hello response: {}", response.getMessage());
            return response.getMessage();
        } catch (Exception e) {
            logger.error("Error calling sayHello: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String sayGoodbye(String name, boolean formal) {
        logger.debug("Sending goodbye request with name: {} and formal: {}", name, formal);
        GoodbyeRequest request = GoodbyeRequest.newBuilder()
                .setName(name)
                .setFormal(formal)
                .build();
        
        try {
            GoodbyeReply response = greeterStub.sayGoodbye(request);
            logger.debug("Received goodbye response: {}", response.getMessage());
            return response.getMessage();
        } catch (Exception e) {
            logger.error("Error calling sayGoodbye: {}", e.getMessage(), e);
            throw e;
        }
    }
}
