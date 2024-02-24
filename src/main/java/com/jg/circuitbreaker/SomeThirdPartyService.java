package com.jg.circuitbreaker;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class SomeThirdPartyService {

    @CircuitBreaker(name = "doStuffCircuitBreaker", fallbackMethod = "fallbackMethod")
    public String someIntegrationMethod() {
        if (new Random().nextInt(10) < 5) {
            log.error("third-party service failed!");
            throw new RuntimeException("Some sporadic third-party error.");
        }

        return "Hello World!";
    }

    public String fallbackMethod(final Throwable ex) {
        return "Third-party service is currently unavailable. Please, try again later.";
    }

}
