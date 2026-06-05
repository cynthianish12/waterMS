package com.utilitybilling.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI configuration with JWT bearer authentication. */
@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Utility Billing System API").version("1.0.0")
                        .description("WASAC/REG backend. Endpoint summaries list allowed roles."))
                .addTagsItem(new Tag().name("01. Authentication").description("Customer signup, OTP verification, login, refresh, logout, and password recovery."))
                .addTagsItem(new Tag().name("02. Admin User Management").description("Admin user management, activation, and deactivation."))
                .addTagsItem(new Tag().name("03. Customer Management").description("Customer profiles and customer administration."))
                .addTagsItem(new Tag().name("04. Meter Management").description("Assign and view customer meters."))
                .addTagsItem(new Tag().name("05. Meter Reading Management").description("Operator meter reading capture and reading validation."))
                .addTagsItem(new Tag().name("06. Tariff Management").description("Admin tariff configuration and tariff version viewing."))
                .addTagsItem(new Tag().name("07. Bill Management").description("Bill generation, bill approval, and customer bill viewing."))
                .addTagsItem(new Tag().name("08. Payment Management").description("Finance payment recording and customer payment history."))
                .addTagsItem(new Tag().name("09. Notifications").description("Customer and billing notifications."))
                .addTagsItem(new Tag().name("10. Audit Logs").description("System audit log viewing."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().name("bearerAuth").type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }
}
