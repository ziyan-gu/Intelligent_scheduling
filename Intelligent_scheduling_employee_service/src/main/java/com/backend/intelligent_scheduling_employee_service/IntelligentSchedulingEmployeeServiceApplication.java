package com.backend.intelligent_scheduling_employee_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class IntelligentSchedulingEmployeeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentSchedulingEmployeeServiceApplication.class, args);
    }

}
