package com.example.userservicemodule.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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