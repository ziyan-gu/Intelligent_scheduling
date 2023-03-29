package com.backend.intelligent_scheduling_user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class IntelligentSchedulingUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentSchedulingUserServiceApplication.class, args);
    }

}
