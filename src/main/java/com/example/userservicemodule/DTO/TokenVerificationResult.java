package com.example.userservicemodule.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Clase que encapsula el resultado de una verificación de token
 */
@Data
public class TokenVerificationResult {
    private Integer userId;
    private String email;
    private LocalDateTime expiresAt;
}
