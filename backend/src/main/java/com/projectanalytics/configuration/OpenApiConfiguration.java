package com.projectanalytics.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger documentation configuration with JWT bearer scheme.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI projectAnalyticsOpenApi(
            @Value("${projectanalytics.info.version}") String version
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Analytics API")
                        .description(
                                "REST API for Project Analytics — Decision Intelligence Platform for OpenProject. "
                                        + "Protected endpoints require JWT Bearer authentication."
                        )
                        .version(version)
                        .contact(new Contact().name("Project Analytics")))
                .servers(List.of(
                        new Server().url("/").description("Current host")
                ))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
