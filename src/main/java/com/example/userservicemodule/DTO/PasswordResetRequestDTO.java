package com.example.userservicemodule.DTO;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * DTO para solicitud de restablecimiento de contraseña
 */
@Data
public class PasswordResetRequestDTO {
    private String email;

    // URL a la que se redirigirá al usuario (proporcionada por el cliente)
    private String redirectUrl;
}

