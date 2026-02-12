package com.ledger.query_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Value("${server.port}")
    private String port;

    @GetMapping("/ping")
    public String ping() {
        return "Command service responding from port: " + port;
    }

    @GetMapping("/count")
    public String count() {
        int current = COUNTER.incrementAndGet();
        System.out.println("Handled by port " + port + " | count=" + current);
        return "testing";
    }
}

