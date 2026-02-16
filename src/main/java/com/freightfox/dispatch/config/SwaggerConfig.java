package com.freightfox.dispatch.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig - OpenAPI/Swagger documentation configuration
 * 
 * WHAT IS SWAGGER?
 * - Auto-generates interactive API documentation
 * - Discovers all REST endpoints from @RestController classes
 * - Parses request/response DTOs
 * - Extracts validation rules from annotations
 * - Provides "Try it out" feature to test APIs
 * 
 * ACCESS POINTS:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 * 
 * @Configuration:
 * - Marks this as Spring configuration class
 * - Beans defined here are registered in Spring context
 * - Like: module.exports in Node.js config files
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Configure OpenAPI documentation
     * 
     * @Bean:
     * - Creates a Spring-managed bean
     * - Spring calls this method once at startup
     * - Returns configured OpenAPI object
     * 
     * @return OpenAPI configuration object
     */
    @Bean
    public OpenAPI dispatchOpenAPI() {
        return new OpenAPI()
            // API Metadata
            .info(new Info()
                .title("Dispatch Load Balancer API")
                .description(
                    "REST API for optimizing delivery order allocation to vehicles. " +
                    "Features priority-based greedy assignment with distance minimization using Haversine formula."
                )
                .version("1.0.0")
                
                // Contact Information
                .contact(new Contact()
                    .name("FreightFox Engineering")
                    .email("engineering@freightfox.com")
                    .url("https://freightfox.com")
                )
                
                // License Information (optional)
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                )
            )
            
            // Server Configuration
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local Development Server"),
                
                new Server()
                    .url("https://api.freightfox.com")
                    .description("Production Server")
            ))
            
            // External Documentation (optional)
            .externalDocs(new ExternalDocumentation()
                .description("Full API Documentation")
                .url("https://docs.freightfox.com/dispatch-api")
            );
    }
}