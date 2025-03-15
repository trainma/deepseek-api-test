package com.example.deepseekapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DeepseekApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepseekApiApplication.class, args);
    }
}
