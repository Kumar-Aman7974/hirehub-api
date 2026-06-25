package com.hirehub.hirehubapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // Remove .openapi("3.0.1") — SpringDoc handles this automatically
                .info(new Info()
                        .title("HireHub API Documentation")
                        .description("""
                                Complete REST API for HireHub Job Portal System.
                                
                                ## Features
                                - JWT Authentication with Refresh Tokens
                                - User Management (Job Seeker, Employer, Admin)
                                - Job Posting with Search & Filters
                                - Application Management with Status Tracking
                                - Asynchronous Email Notifications
                                - Secure File Upload for Resumes & Logos
                                
                                ## Authentication
                                Use the **Authorize** button above to add your JWT token.
                                All endpoints except `/api/auth/**` require authentication.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Aman Kumar")
                                .email("amanbth7974@gmail.com"))
                );
    }
}

//
////
////    @Bean
////    public OpenAPI openAPI() {
////        return new OpenAPI()
////                .info(new Info()
////                        .title("HireHub API Documentation")
////                        .description("""
////                                Complete REST API for HireHub Job Portal System.
////
////                                ## Features
////                                - JWT Authentication with Refresh Tokens
////                                - User Management (Job Seeker, Employer, Admin)
////                                - Job Posting with Search & Filters
////                                - Application Management with Status Tracking
////                                - Asynchronous Email Notifications
////                                - Secure File Upload for Resumes & Logos
////
////                                ## Authentication
////                                Use the **Authorize** button above to add your JWT token.
////                                All endpoints except `/api/auth/**` require authentication.
////                                """)
////                        .version("3.0.1")
////                        .contact(new Contact()
////                                .name("Aman Kumar")
////                                .email("amanbth7974@gmail.com")
////                                .url("https://github.com/kumar-aman7974"))
////                        .license(new License()
////                                .name("MIT License")
////                                .url("https://opensource.org/licenses/MIT")))
////                .servers(List.of(
////                        new Server()
////                                .url("http://localhost:8080")
////                                .description("Development Server"),
////                        new Server()
////                                .url("https://api.hirehub.com")
////                                .description("Production Server")))
////                .addSecurityItem(new SecurityRequirement()
////                        .addList("Bearer Authentication"))
////                .components(new Components()
////                        .addSecuritySchemes("Bearer Authentication",
////                                new SecurityScheme()
////                                        .name("Bearer Authentication")
////                                        .type(SecurityScheme.Type.HTTP)
////                                        .scheme("bearer")
////                                        .bearerFormat("JWT")
////                                        .description("""
////                                                Enter your JWT token in the format:
////                                                **Bearer <your-token>**
////
////                                                To get a token:
////                                                1. Register: POST /api/auth/register
////                                                2. Login: POST /api/auth/login
////                                                3. Copy the accessToken from response
////                                                4. Paste it here as: Bearer <token>
////                                                """)));
////    }
//}