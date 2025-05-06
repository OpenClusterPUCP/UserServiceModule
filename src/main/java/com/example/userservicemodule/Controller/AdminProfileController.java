package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controlador para la gestión de perfiles de usuario en el módulo UserService
 */
@RestController
@RequestMapping("/Admin/user")
@Slf4j
public class AdminProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Obtiene la información del perfil de un usuario
     *
     * @param id ID del usuario
     * @return Datos del perfil del usuario
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Integer id) {
        try {
            log.info("Solicitando información de perfil para usuario ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("name", user.getName());
            response.put("lastname", user.getLastname());
            response.put("code", user.getCode());
            response.put("role", user.getRole().getName());
            response.put("roleId", user.getRole().getId());
            response.put("state", user.getState());

            // Manejar campos de fecha que pueden ser null
            if (user.getCreatedAt() != null) {
                response.put("createdAt", user.getCreatedAt().format(formatter));
            } else {
                response.put("createdAt", null);
            }

            if (user.getLastLogin() != null) {
                response.put("lastLogin", user.getLastLogin().format(formatter));
            } else {
                response.put("lastLogin", null);
            }

            log.debug("Información de perfil de usuario ID {} recuperada exitosamente", id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener información de perfil para usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener información de perfil: " + e.getMessage()));
        }
    }

    /**
     * Actualiza la información del perfil de un usuario
     *
     * @param profileData Datos del perfil a actualizar
     * @return Resultado de la operación
     */
    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> profileData) {
        try {
            log.info("Actualizando perfil de usuario: {}", profileData.get("username"));

            // Verificar datos requeridos
            if (!profileData.containsKey("id") || !profileData.containsKey("username") ||
                    !profileData.containsKey("name") || !profileData.containsKey("lastname")) {
                log.warn("Datos de perfil incompletos");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Datos de perfil incompletos"));
            }

            Integer userId = (Integer) profileData.get("id");

            // Obtener usuario
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Verificar si el nombre de usuario es único (si se está cambiando)
            String newUsername = (String) profileData.get("username");
            if (!newUsername.equals(user.getUsername()) &&
                    userRepository.findByUsername(newUsername).isPresent()) {
                log.warn("Nombre de usuario ya existe: {}", newUsername);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "El nombre de usuario ya existe"));
            }

            // Actualizar datos
            user.setUsername(newUsername);
            user.setName((String) profileData.get("name"));
            user.setLastname((String) profileData.get("lastname"));

            // Guardar cambios
            User updatedUser = userRepository.save(user);
            log.info("Perfil de usuario ID {} actualizado exitosamente", userId);

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("name", updatedUser.getName());
            response.put("lastname", updatedUser.getLastname());
            response.put("message", "Perfil actualizado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Error al actualizar perfil: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar perfil: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al actualizar perfil: " + e.getMessage()));
        }
    }

    /**
     * Cambia la contraseña de un usuario
     *
     * @param passwordData Datos para el cambio de contraseña
     * @return Resultado de la operación
     */
    @PostMapping("/profile/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> passwordData) {
        try {
            log.info("Cambiando contraseña para usuario: {}", passwordData.get("username"));

            // Verificar datos requeridos
            if (!passwordData.containsKey("username") || !passwordData.containsKey("currentPassword") ||
                    !passwordData.containsKey("newPassword")) {
                log.warn("Datos de cambio de contraseña incompletos");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Datos de cambio de contraseña incompletos"));
            }

            String username = (String) passwordData.get("username");
            String currentPassword = (String) passwordData.get("currentPassword");
            String newPassword = (String) passwordData.get("newPassword");

            // Obtener usuario
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + username));

            // Verificar contraseña actual
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("Contraseña actual incorrecta para usuario: {}", username);
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "La contraseña actual es incorrecta"));
            }

            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Contraseña actualizada exitosamente para usuario: {}", username);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (NoSuchElementException e) {
            log.warn("Error al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al cambiar contraseña: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al cambiar contraseña: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas y estadísticas de actividad del administrador
     *
     * @param id ID del usuario administrador
     * @return Métricas de actividad del administrador
     */
    @GetMapping("/profile/metrics/{id}")
    public ResponseEntity<?> getAdminMetrics(@PathVariable Integer id) {
        try {
            log.info("Obteniendo métricas para administrador ID: {}", id);

            // Verificar que el usuario existe
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Verificar que el usuario es administrador
            if (user.getRole().getId() != 2) { // Asumiendo que ID 2 corresponde al rol de Admin
                log.warn("El usuario ID {} no es administrador", id);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "El usuario no tiene permisos de administrador"));
            }

            // En una implementación real, aquí se consultarían bases de datos para obtener
            // métricas reales como número de slices administrados, usuarios gestionados, etc.
            // Por ahora usaremos datos de ejemplo para la interfaz

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("slicesManaged", 12);
            metrics.put("usersManaged", 24);
            metrics.put("vmsCreated", 56);

            // Actividades recientes (en una implementación real se obtendrían de una tabla de logs)
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("type", "slice_creation");
            activity1.put("description", "Creación de nuevo Slice");
            activity1.put("timestamp", "Hace 2 horas");

            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("type", "user_creation");
            activity2.put("description", "Usuario añadido al sistema");
            activity2.put("timestamp", "Ayer");

            Map<String, Object> activity3 = new HashMap<>();
            activity3.put("type", "resource_config");
            activity3.put("description", "Configuración de recursos");
            activity3.put("timestamp", "Hace 3 días");

            // Datos de seguridad
            Map<String, Object> security = new HashMap<>();
            security.put("lastPasswordChange", "Hace 30 días");
            security.put("activeSessions", 1);

            // Combinar todo en la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("metrics", metrics);
            response.put("recentActivities", new Object[]{activity1, activity2, activity3});
            response.put("security", security);

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Error al obtener métricas: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener métricas para administrador ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener métricas de administrador: " + e.getMessage()));
        }
    }
}
