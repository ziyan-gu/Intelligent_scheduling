package com.backend.intelligent_scheduling_user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class IntelligentSchedulingLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentSchedulingLoginApplication.class, args);
    }

}
