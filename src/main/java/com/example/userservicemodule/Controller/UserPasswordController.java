package com.example.userservicemodule.Controller;

import com.example.userservicemodule.DTO.PasswordResetDTO;
import com.example.userservicemodule.DTO.PasswordResetRequestDTO;
import com.example.userservicemodule.DTO.PasswordResetResult;
import com.example.userservicemodule.DTO.TokenVerificationResult;
import com.example.userservicemodule.Service.UserPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para operaciones relacionadas con restablecimiento de contraseñas.
 */
@RestController
@RequestMapping("/api/v1/users/password")
@Slf4j
public class UserPasswordController {

    private final UserPasswordService passwordService;
    private final RestTemplate restTemplate;

    @Value("${api-gateway.url:http://localhost:8090}")
    private String apiGatewayUrl;

    @Autowired
    public UserPasswordController(UserPasswordService passwordService, RestTemplate restTemplate) {
        this.passwordService = passwordService;
        this.restTemplate = restTemplate;
    }

    /**
     * Endpoint para solicitar un restablecimiento de contraseña.
     * Genera un token único y envía un correo al usuario.
     *
     * @param request La solicitud con el email del usuario
     * @return Resultado de la operación
     */
    @PostMapping("/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDTO request) {
        log.info("Recibida solicitud de restablecimiento de contraseña para: {}", request.getEmail());

        try {
            // Verificar que el email existe
            if (!passwordService.isValidUserEmail(request.getEmail())) {
                log.warn("Email no encontrado: {}", request.getEmail());
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", "error",
                                "message", "El correo electrónico no está registrado en el sistema"
                        ));
            }

            // Generar token de restablecimiento
            String token = passwordService.generateResetToken(request.getEmail());

            // Crear enlace con la URL de redirección proporcionada
            String resetLink = request.getRedirectUrl() + "?token=" + token;

            // Obtener información del usuario para personalizar el correo
            String username = passwordService.getUsernameByEmail(request.getEmail());

            // Enviar correo electrónico con el enlace utilizando el EmailServiceModule
            try {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("to", request.getEmail());
                params.add("resetLink", resetLink);
                if (username != null) {
                    params.add("username", username);
                }

                // Enviar la solicitud al servicio de correo
                ResponseEntity<Map<String, Object>> emailResponse = restTemplate.exchange(
                        apiGatewayUrl + "/api/email/send/password-reset",
                        HttpMethod.POST,
                        new HttpEntity<>(params),
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );

                if (emailResponse.getStatusCode().is2xxSuccessful()) {
                    // Respuesta exitosa
                    return ResponseEntity.ok(Map.of(
                            "status", "success",
                            "message", "Se ha enviado un enlace de restablecimiento a tu correo electrónico"
                    ));
                } else {
                    log.error("Error al enviar correo de restablecimiento: servicio de correo respondió con estado {}", emailResponse.getStatusCode());
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of(
                                    "status", "error",
                                    "message", "Error al enviar el correo electrónico. Por favor, inténtalo más tarde."
                            ));
                }
            } catch (Exception e) {
                log.error("Error al enviar correo de restablecimiento: {}", e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "status", "error",
                                "message", "Error al enviar el correo electrónico. Por favor, inténtalo más tarde."
                        ));
            }
        } catch (Exception e) {
            log.error("Error al procesar solicitud de restablecimiento: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Error en el servidor al procesar la solicitud"
                    ));
        }
    }

    /**
     * Endpoint para verificar si un token de restablecimiento es válido.
     *
     * @param token Token de restablecimiento
     * @return Información sobre la validez del token
     */
    @GetMapping("/verify-token/{token}")
    public ResponseEntity<?> verifyResetToken(@PathVariable String token) {
        log.info("Verificando token de restablecimiento");

        try {
            // Verificar token
            Optional<TokenVerificationResult> result = passwordService.verifyResetToken(token);

            if (result.isPresent()) {
                TokenVerificationResult verificationResult = result.get();

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "valid", true,
                        "email", verificationResult.getEmail(),
                        "userId", verificationResult.getUserId(),
                        "expiresAt", verificationResult.getExpiresAt().toString()
                ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", "error",
                                "valid", false,
                                "message", "Token inválido o expirado"
                        ));
            }
        } catch (Exception e) {
            log.error("Error al verificar token: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "valid", false,
                            "message", "Error al verificar el token"
                    ));
        }
    }

    /**
     * Endpoint para cambiar la contraseña utilizando un token de restablecimiento.
     *
     * @param request La solicitud con el token y la nueva contraseña
     * @return Resultado de la operación
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO request) {
        log.info("Procesando restablecimiento de contraseña");

        try {
            // Verificar que se proporcionaron los datos necesarios
            if (request.getToken() == null || request.getNewPassword() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", "error",
                                "message", "Token y nueva contraseña son requeridos"
                        ));
            }

            // Verificar longitud mínima de contraseña
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", "error",
                                "message", "La contraseña debe tener al menos 6 caracteres"
                        ));
            }

            // Intentar cambiar la contraseña
            PasswordResetResult result = passwordService.resetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );

            if (result.isSuccess()) {
                // Notificar al usuario por correo que su contraseña ha sido cambiada
                try {
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("to", result.getEmail());
                    if (result.getUsername() != null) {
                        params.add("username", result.getUsername());
                    }

                    // Enviar la solicitud al servicio de correo
                    restTemplate.exchange(
                            apiGatewayUrl + "/api/email/send/password-changed",
                            HttpMethod.POST,
                            new HttpEntity<>(params),
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

                    // Continuamos incluso si hay error en el envío del correo
                } catch (Exception e) {
                    log.warn("No se pudo enviar la notificación de cambio de contraseña: {}", e.getMessage());
                }

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Contraseña actualizada correctamente"
                ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", "error",
                                "message", result.getErrorMessage()
                        ));
            }
        } catch (Exception e) {
            log.error("Error al restablecer la contraseña: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Error en el servidor al restablecer la contraseña"
                    ));
        }
    }
}