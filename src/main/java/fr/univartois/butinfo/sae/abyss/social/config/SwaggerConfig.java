package fr.univartois.butinfo.sae.abyss.social.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * This class defines the OpenAPI bean that configures the API documentation for the application, including the title and description of the API.
 * The @Configuration annotation indicates that this class provides bean definitions for the application context, and the @Bean annotation on the customOpenAPI method indicates that it returns a bean to be managed by the Spring container.
 * The OpenAPI bean is configured with an Info object that sets the title and description of the API, which will be displayed in the generated API documentation when accessed through the Swagger UI or other OpenAPI-compatible tools.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the OpenAPI bean for configuring API documentation.
     * @return An OpenAPI object configured with the title and a description, which will be used to generate API documentation for the application.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Abyss Social API")
                        .description("API pour la gestion des utilisateurs et du réseau social"));
    }
}
