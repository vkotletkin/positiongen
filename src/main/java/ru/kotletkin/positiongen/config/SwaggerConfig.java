package ru.kotletkin.positiongen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI positiongen() {
        return new OpenAPI()
                .info(new Info()
                        .title("Positiongen")
                        .version("v1"));
    }
}
