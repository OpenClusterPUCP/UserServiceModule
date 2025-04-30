package com.example.userservicemodule.DTO;

import lombok.Data;

/**
 * DTO para el formulario de restablecimiento de contraseña
 */
@Data
public class PasswordResetDTO {
    private String token;

    private String newPassword;
}
