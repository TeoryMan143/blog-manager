package com.teoryman.blogmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("Blog Manager API")
                    .version("1.0.0")
                    .description("A scalable blog management system with users, posts, and comments")
                    .contact(new Contact()
                            .name("Blog Manager Support")
                            .email("jonathancortestm143@gmail.com")
                            .url("https://portfoliotm143.vercel.app/")))
            .components(new Components()
                    .addSecuritySchemes("Bearer Authentication",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("JWT Token for Bearer authentication")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
  }
}

