package com.example.userservicemodule.Service;

import com.example.userservicemodule.DTO.PasswordResetResult;
import com.example.userservicemodule.DTO.TokenVerificationResult;
import com.example.userservicemodule.Entity.PasswordResetToken;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.PasswordTokenRepository;
import com.example.userservicemodule.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para operaciones relacionadas con contraseñas de usuarios.
 */
@Service
@Slf4j
public class UserPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Tiempo de expiración del token de restablecimiento (en horas)
     */
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    /**
     * Verifica si existe un usuario con el email proporcionado.
     *
     * @param email Email a verificar
     * @return true si el email corresponde a un usuario, false en caso contrario
     */
    public boolean isValidUserEmail(String email) {
        return userRepository.findByUsername(email).isPresent();
    }

    /**
     * Obtiene el nombre de usuario por su email (que puede ser el mismo que el email).
     *
     * @param email Email del usuario
     * @return El nombre del usuario o null si no se encuentra
     */
    public String getUsernameByEmail(String email) {
        Optional<User> user = userRepository.findByUsername(email);
        return user.map(u -> {
            // Intentar obtener nombre completo
            StringBuilder fullName = new StringBuilder();
            if (u.getName() != null && !u.getName().isEmpty()) {
                fullName.append(u.getName());
            }
            if (u.getLastname() != null && !u.getLastname().isEmpty()) {
                if (fullName.length() > 0) {
                    fullName.append(" ");
                }
                fullName.append(u.getLastname());
            }

            // Si no hay nombre completo, usar el username
            if (fullName.length() == 0) {
                return u.getUsername();
            }

            return fullName.toString();
        }).orElse(null);
    }

    /**
     * Genera un token de restablecimiento de contraseña para el usuario.
     *
     * @param email Email del usuario
     * @return Token generado
     */
    @Transactional
    public String generateResetToken(String email) {
        log.info("Generando token de restablecimiento para: {}", email);

        // Buscar al usuario por email
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));

        // Generar token aleatorio
        String token = UUID.randomUUID().toString();

        // Calcular fecha de expiración
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        // Eliminar tokens anteriores para este usuario
        passwordTokenRepository.deleteByUserId(user.getId());

        // Crear y guardar el nuevo token
        PasswordResetToken passwordToken = new PasswordResetToken();
        passwordToken.setToken(token);
        passwordToken.setUser(user);
        passwordToken.setExpiryDate(expirationDate);
        passwordToken.setCreatedAt(LocalDateTime.now());
        passwordTokenRepository.save(passwordToken);

        log.info("Token generado correctamente para: {}", email);
        return token;
    }

    /**
     * Verifica si un token de restablecimiento es válido.
     *
     * @param token Token a verificar
     * @return Resultado de la verificación con información del usuario
     */
    public Optional<TokenVerificationResult> verifyResetToken(String token) {
        log.info("Verificando token de restablecimiento");

        Optional<PasswordResetToken> passwordToken = passwordTokenRepository.findByToken(token);

        if (passwordToken.isPresent()) {
            PasswordResetToken resetToken = passwordToken.get();

            // Verificar si el token ha expirado
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                log.info("Token expirado");
                return Optional.empty();
            }

            User user = resetToken.getUser();

            TokenVerificationResult result = new TokenVerificationResult();
            result.setUserId(user.getId());
            result.setEmail(user.getUsername());
            result.setExpiresAt(resetToken.getExpiryDate());

            return Optional.of(result);
        }

        log.info("Token no encontrado o inválido");
        return Optional.empty();
    }

    /**
     * Restablece la contraseña del usuario utilizando un token.
     *
     * @param token Token de restablecimiento
     * @param newPassword Nueva contraseña
     * @return Resultado de la operación
     */
    @Transactional
    public PasswordResetResult resetPassword(String token, String newPassword) {
        log.info("Procesando restablecimiento de contraseña con token");

        Optional<PasswordResetToken> passwordToken = passwordTokenRepository.findByToken(token);

        if (passwordToken.isEmpty()) {
            log.info("Token no encontrado");
            return PasswordResetResult.error("Token inválido o expirado");
        }

        PasswordResetToken resetToken = passwordToken.get();

        // Verificar si el token ha expirado
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.info("Token expirado");
            return PasswordResetResult.error("El token ha expirado");
        }

        // Obtener usuario
        User user = resetToken.getUser();

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Eliminar el token utilizado
        passwordTokenRepository.delete(resetToken);

        log.info("Contraseña restablecida correctamente para el usuario: {}", user.getUsername());

        // Construir resultado con información del usuario
        String username = user.getName() != null ? user.getName() : user.getUsername();

        return PasswordResetResult.success(user.getUsername(), username);
    }
}
