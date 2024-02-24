# Introduction

A circuit breaker helps fallback to an alternative function when an intended function starts failing..

For example, a third-party integration start failing. When a failure threshold is reached the circuit-breaker will "open" and would instead call a fallback function that instead returns a warning that "the third-party API is currently unavailable. Please, try again later.".

# Circuit Breaker states
## Closed

In the closed state, the circuit breaker allows all requests to pass through to the underlying system.
During this state, the circuit breaker monitors the responses from the underlying system.
If the failure rate (percentage of failed requests) is below a certain threshold and the number of requests exceeds a minimum threshold (to prevent the circuit breaker from opening too soon), the circuit breaker remains closed.
If the failure rate exceeds the threshold, the circuit breaker transitions to the open state to prevent further calls to the failing system.

## Open

In the open state, the circuit breaker prevents requests from being passed through to the underlying system.
The circuit breaker "trips" into the open state when the failure rate exceeds a predefined threshold.
While in the open state, the circuit breaker may redirect calls to a fallback mechanism, such as returning cached data, responding with a default value, or displaying an error message.
The circuit breaker remains in the open state for a specified duration, allowing the underlying system time to recover.

## Half-Open

After the open state duration elapses, the circuit breaker transitions to the half-open state.
In the half-open state, the circuit breaker allows a limited number of requests to pass through to the underlying system.
The purpose of the half-open state is to test if the underlying system has recovered from its failure. By allowing a controlled number of requests, the circuit breaker can gauge the system's responsiveness.
Depending on the responses from the underlying system during the half-open state, the circuit breaker may transition back to the closed state if the system appears to have recovered, or it may return to the open state if the system is still experiencing issues.

# Dependency
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

# Implementation
## Note: 
Had to add 
```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
``` 
for @CircuitBreaker proxy to work, including the following dependencies:

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>
```

```java
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
```

This code means that when there's an amount o ffailures greater than some threshold within a frame, the fallbackMethod will be called instead.

See the below properties for how this CircuitBreaker is configured.

```yaml
management:
  security:
    enabled: false
  health:
    circuitbreakers:
      enabled: true
  endpoint:
    health:
      show-details: "ALWAYS"
  endpoints:
    web:
      exposure:
        include: "*"

resilience4j.circuitbreaker:
  configs:
    default:
      registerHealthIndicator: true # exposes the circuit breakers over actuator endpoints.
      slidingWindowSize: 10 # monitors last 10 attempts
      minimumNumberOfCalls: 5 # waits for at least 5 calls before starting monitoring the window size
      permittedNumberOfCallsInHalfOpenState: 3 # allowed calls in HALF_OPEN state.
      automaticTransitionFromOpenToHalfOpenEnabled: true # allows switching from OPEN to HALF_OPEN based on time (waitDurationInOpenState)
      waitDurationInOpenState: 5s # time to wait in OPEN state before switching to HALF_OPEN
      failureRateThreshold: 50 # if 50% or more of calls fail, the circuit breaker will open.
      eventConsumerBufferSize: 50
  instances:
    doStuffCircuitBreaker:
      baseConfig: default
```