package com.jg.circuitbreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Service
@RestController
@RequiredArgsConstructor
public class SomeRestController {

    private final SomeThirdPartyService someThirdPartyService;
    private final ApplicationContext applicationContext;

    @GetMapping("/test")
    public String controllerMethod() {
        return someThirdPartyService.someIntegrationMethod();
    }

    @GetMapping("/beans")
    public void getBeans() {
        System.out.println();
    }

}
