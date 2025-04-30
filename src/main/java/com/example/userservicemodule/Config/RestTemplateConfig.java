package com.example.userservicemodule.Config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

import java.util.Arrays;

/**
 * Configuración para RestTemplate utilizado en la comunicación con otros servicios.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Proporciona una instancia de RestTemplate para realizar peticiones HTTP
     * a otros microservicios a través del API Gateway.
     *
     * @return Instancia de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



}