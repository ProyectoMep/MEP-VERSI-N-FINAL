package com.example.colegiosapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Colegios Spring Boot application.  This class
 * bootstraps the Spring context and starts the embedded web server.
 */
@SpringBootApplication
public class ColegiosAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColegiosAppApplication.class, args);
    }
}