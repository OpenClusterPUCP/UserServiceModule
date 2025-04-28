package com.example.userservicemodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

public class UserServiceModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceModuleApplication.class, args);
    }

}
