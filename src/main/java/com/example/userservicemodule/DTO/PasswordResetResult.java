package com.example.userservicemodule.DTO;

import lombok.Data;

/**
 * Clase que encapsula el resultado de un restablecimiento de contraseña
 */
@Data
public class PasswordResetResult {
    private boolean success;
    private String email;
    private String username;
    private String errorMessage;

    private PasswordResetResult(boolean success, String email, String username, String errorMessage) {
        this.success = success;
        this.email = email;
        this.username = username;
        this.errorMessage = errorMessage;
    }

    public static PasswordResetResult success(String email, String username) {
        return new PasswordResetResult(true, email, username, null);
    }

    public static PasswordResetResult error(String errorMessage) {
        return new PasswordResetResult(false, null, null, errorMessage);
    }
}