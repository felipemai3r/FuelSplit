package com.caronatracker.trips.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trips & Charges Service API")
                        .description("API para gerenciamento de viagens e divisão de custos de combustível")
                        .version("1.0.0"));
    }
}
