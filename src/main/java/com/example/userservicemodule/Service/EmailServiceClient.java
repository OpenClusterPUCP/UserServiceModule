package com.example.userservicemodule.Service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Cliente Feign para comunicarse con el servicio de correo electrónico.
 * Define los endpoints del EmailServiceModule que necesitamos consumir.
 */
@FeignClient(name = "EmailServiceModule", path = "/api/v1/emails")
public interface EmailServiceClient {

    /**
     * Envía un correo electrónico con un enlace para restablecer la contraseña.
     *
     * @param to Email del destinatario
     * @param resetLink Enlace de restablecimiento
     * @param username Nombre del usuario (opcional)
     * @return Respuesta con el resultado del envío
     */
    @PostMapping("/send/password-reset")
    ResponseEntity<Map<String, Object>> sendPasswordResetEmail(
            @RequestParam("to") String to,
            @RequestParam("resetLink") String resetLink,
            @RequestParam(value = "username", required = false) String username);

    /**
     * Envía una notificación de que la contraseña ha sido cambiada.
     *
     * @param to Email del destinatario
     * @param username Nombre del usuario (opcional)
     * @return Respuesta con el resultado del envío
     */
    @PostMapping("/send/password-changed")
    ResponseEntity<Map<String, Object>> sendPasswordChangedNotification(
            @RequestParam("to") String to,
            @RequestParam(value = "username", required = false) String username);
}
