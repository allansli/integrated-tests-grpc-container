package com.example.grpcclient;

import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(
    classes = GrpcClientApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true"
})
public class CucumberTestConfig {
    private static final Logger logger = LoggerFactory.getLogger(CucumberTestConfig.class);
    
    static {
        // Ensure system properties are set before Spring context is created
        logger.info("CucumberTestConfig static initializer - setting system properties");
        
        // Log current system properties
        System.getProperties().forEach((key, value) -> {
            if (key.toString().contains("grpc")) {
                logger.info("System property: {} = {}", key, value);
            }
        });
    }
}
