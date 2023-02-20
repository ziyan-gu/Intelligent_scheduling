package com.backend.intelligent_scheduling_login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
public class IntelligentSchedulingLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentSchedulingLoginApplication.class, args);
    }

}
