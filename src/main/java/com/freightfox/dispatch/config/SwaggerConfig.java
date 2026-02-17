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
 
 * 
 * @Configuration:
 
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Configure OpenAPI documentation
     * 
     * @Bean:
    
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