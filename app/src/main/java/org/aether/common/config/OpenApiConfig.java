package org.aether.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aetherOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aether AI Gateway API")
                        .version("v1")
                        .description("Registry and governance APIs for the Aether AI gateway."));
    }
}
