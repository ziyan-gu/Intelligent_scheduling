package com.backend.intelligent_scheduling_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IntelligentSchedulingGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentSchedulingGatewayApplication.class, args);
    }

}
