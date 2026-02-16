package com.freightfox.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Entry Point
 * 
 * @SpringBootApplication is a meta-annotation combining:
 *                        - @Configuration: Marks this as a configuration class
 *                        - @EnableAutoConfiguration: Auto-configures Spring
 *                        based on dependencies
 *                        - @ComponentScan: Scans
 *                        for @Controller, @Service, @Repository classes
 * 
 *                        Think of it like:
 *                        const express = require('express');
 *                        const app = express();
 *                        app.use(express.json());
 *                        app.use('/api', routes);
 *                        app.listen(8080);
 */
@SpringBootApplication
public class DispatchApplication {

    public static void main(String[] args) {
        // This single line:
        // 1. Starts embedded Tomcat server (like app.listen())
        // 2. Initializes Spring container (dependency injection)
        // 3. Auto-configures database connections
        // 4. Scans for REST controllers
        SpringApplication.run(DispatchApplication.class, args);
    }
}
