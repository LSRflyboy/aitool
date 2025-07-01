package com.aitool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.aitool.config.StorageProperties;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(StorageProperties.class)
public class AIToolBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIToolBackendApplication.class, args);
    }
} 