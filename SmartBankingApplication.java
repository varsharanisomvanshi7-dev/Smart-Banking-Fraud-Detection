package com.bank.frauddetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Smart Banking & Fraud Anomaly Detection System
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableAsync
public class SmartBankingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartBankingApplication.class, args);
    }
}
