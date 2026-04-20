package com.jobmatch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application
 * Job Matching Platform - Production-level implementation
 */
@SpringBootApplication
@Configuration
@EnableAsync
@EnableScheduling
@EnableCaching
public class JobMatchingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobMatchingPlatformApplication.class, args);
    }
}
