package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Beans.ErrorResponse;
import com.example.userservicemodule.BeansRequest.FlavorRequest;
import com.example.userservicemodule.Entity.Flavor;
import com.example.userservicemodule.Entity.Role;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.FlavorRepository;
import com.example.userservicemodule.Repository.RoleRepository;
import com.example.userservicemodule.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Controlador REST para operaciones de administración de usuarios.
 * Proporciona endpoints para la gestión completa de usuarios en el sistema.
 */
@RestController
@RequestMapping("/Admin")
@Slf4j
public class AdminController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FlavorRepository flavorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           FlavorRepository flavorRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.flavorRepository = flavorRepository;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Lista de usuarios con sus datos básicos
     */
    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsers() {
        try {
            log.info("Solicitando lista de todos los usuarios");
            // Check if repository is available
            if (userRepository == null) {
                log.error("Repositorio de usuarios no disponible");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("X-Error-Type", "RepositoryUnavailable")
                        .body("User repository is not available");
            }

            // Get all users and handle empty result
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                log.warn("No se encontraron usuarios en la base de datos");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .header("X-Info", "No users found in the database")
                        .build();
            }

            // Prepare the response
            ArrayList<LinkedHashMap<String, Object>> listaContent = new ArrayList<>();
            for (User user : users) {
                LinkedHashMap<String, Object> userContent = new LinkedHashMap<>();
                userContent.put("id", user.getId());
                userContent.put("name", user.getName());
                userContent.put("lastname", user.getLastname());
                userContent.put("username", user.getUsername());
                userContent.put("role", user.getRole().getName());
                userContent.put("code", user.getCode());
                userContent.put("state", user.getState());
                userContent.put("createdAt", user.getCreatedAt().format(formatter));
                userContent.put("lastLogin", user.getLastLogin().format(formatter));
                listaContent.add(userContent);
            }

            log.debug("Se recuperaron {} usuarios", users.size());
            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(listaContent);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("X-Total-Count", String.valueOf(listaContent.size()))
                    .header("Content-Type", "application/json")
                    .header("Content-Length", String.valueOf(responseBody.getBytes(StandardCharsets.UTF_8).length))
                    .body(responseBody);

        } catch (DataAccessException ex) {
            // Handle database access errors
            log.error("Error de acceso a la base de datos: {}", ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("X-Error-Type", "DatabaseError")
                    .header("X-Error-Message", ex.getMessage())
                    .body("Database access error occurred");
        } catch (Exception ex) {
            // Handle all other unexpected errors
            log.error("Error inesperado al obtener usuarios: {}", ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "UnexpectedError")
                    .header("X-Error-Message", ex.getMessage())
                    .body("An unexpected error occurred");
        }
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario a consultar
     * @return Datos detallados del usuario solicitado
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            log.info("Solicitando información del usuario ID: {}", id);
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
            response.put("createdAt", user.getCreatedAt());
            response.put("lastLogin", user.getLastLogin());

            log.debug("Usuario ID {} recuperado: {}", id, user.getUsername());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al obtener información del usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener información del usuario: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param userData Datos del nuevo usuario
     * @return Datos del usuario creado
     */
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        try {
            log.info("Creando nuevo usuario: {}", userData.get("username"));

            // Verificar si se debe generar una contraseña automáticamente
            boolean generatePassword = userData.containsKey("generatePassword") &&
                    Boolean.TRUE.equals(userData.get("generatePassword"));

            // Validar datos requeridos
            if (!userData.containsKey("username") ||
                    (!generatePassword && !userData.containsKey("password")) ||
                    !userData.containsKey("name") || !userData.containsKey("lastname") ||
                    !userData.containsKey("code") || !userData.containsKey("role")) {
                log.warn("Intento de creación de usuario con datos incompletos");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datos de usuario incompletos");
            }

            // Verificar si el usuario ya existe
            String username = (String) userData.get("username");
            if (userRepository.findByUsername(username).isPresent()) {
                log.warn("Intento de creación de usuario con nombre de usuario existente: {}", username);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("El nombre de usuario ya existe");
            }

            // Obtener el rol
            Map<String, Object> roleData = (Map<String, Object>) userData.get("role");
            Integer roleId = (Integer) roleData.get("id");
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + roleId));

            // Crear el nuevo usuario
            User newUser = new User();
            newUser.setUsername((String) userData.get("username"));

            // Generar contraseña si es necesario
            String password;
            if (generatePassword) {
                password = generateSecurePassword(12);
                log.info("CONTRASEÑA GENERADA para usuario {}: {}", username, password);
                userData.put("password", password); // Actualizar en userData para incluirla en la respuesta
            } else {
                password = (String) userData.get("password");
            }

            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setName((String) userData.get("name"));
            newUser.setLastname((String) userData.get("lastname"));
            newUser.setCode((String) userData.get("code"));
            newUser.setRole(role);
            newUser.setState(userData.containsKey("state") ? (String) userData.get("state") : "1");
            newUser.setCreatedAt(LocalDateTime.now());

            // Guardar el usuario
            User savedUser = userRepository.save(newUser);
            log.info("Usuario creado exitosamente: ID {} - {}", savedUser.getId(), savedUser.getUsername());

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("name", savedUser.getName());
            response.put("lastname", savedUser.getLastname());
            response.put("code", savedUser.getCode());
            response.put("role", savedUser.getRole().getName());
            response.put("state", savedUser.getState());
            response.put("createdAt", savedUser.getCreatedAt());
            response.put("message", "Usuario creado exitosamente");

            // Solo incluir la contraseña generada en la respuesta si se generó automáticamente
            if (generatePassword) {
                response.put("generatedPassword", password);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error de argumentos al crear usuario: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage());
        }
    }

    /**
     * Genera una contraseña aleatoria segura
     * @param length Longitud de la contraseña
     * @return Contraseña generada
     */
    private String generateSecurePassword(int length) {
        // Caracteres permitidos
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?";
        String allChars = upperCase + lowerCase + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Asegurar al menos un carácter de cada tipo
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Completar el resto de la contraseña
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Mezclar los caracteres
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }


    /**
     * Actualiza un usuario existente.
     *
     * @param id ID del usuario a actualizar
     * @param userData Nuevos datos del usuario
     * @return Resultado de la actualización
     */
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> userData) {
        try {
            log.info("Actualizando usuario ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Actualizar datos del usuario (solo los campos que vienen en la petición)
            if (userData.containsKey("name")) {
                user.setName((String) userData.get("name"));
                log.debug("Actualizando nombre de usuario ID {}: {}", id, userData.get("name"));
            }

            if (userData.containsKey("lastname")) {
                user.setLastname((String) userData.get("lastname"));
                log.debug("Actualizando apellido de usuario ID {}: {}", id, userData.get("lastname"));
            }

            if (userData.containsKey("code")) {
                user.setCode((String) userData.get("code"));
                log.debug("Actualizando código de usuario ID {}: {}", id, userData.get("code"));
            }

            if (userData.containsKey("username")) {
                String newUsername = (String) userData.get("username");
                if (!newUsername.equals(user.getUsername()) &&
                        userRepository.findByUsername(newUsername).isPresent()) {
                    log.warn("Intento de actualizar usuario ID {} con nombre de usuario existente: {}", id, newUsername);
                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body("El nombre de usuario ya existe");
                }
                user.setUsername(newUsername);
                log.debug("Actualizando nombre de usuario ID {}: {}", id, newUsername);
            }

            if (userData.containsKey("password")) {
                user.setPassword(passwordEncoder.encode((String) userData.get("password")));
                log.debug("Actualizando contraseña de usuario ID {}", id);
            }

            if (userData.containsKey("role")) {
                Map<String, Object> roleData = (Map<String, Object>) userData.get("role");
                Integer roleId = (Integer) roleData.get("id");
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + roleId));
                user.setRole(role);
                log.debug("Actualizando rol de usuario ID {} a: {}", id, role.getName());
            }

            if (userData.containsKey("state")) {
                user.setState((String) userData.get("state"));
                log.debug("Actualizando estado de usuario ID {} a: {}", id, userData.get("state"));
            }

            // Guardar cambios
            User updatedUser = userRepository.save(user);
            log.info("Usuario ID {} actualizado exitosamente", id);

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("name", updatedUser.getName());
            response.put("lastname", updatedUser.getLastname());
            response.put("code", updatedUser.getCode());
            response.put("role", updatedUser.getRole().getName());
            response.put("state", updatedUser.getState());
            response.put("message", "Usuario actualizado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar actualizar: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Error de argumentos al actualizar usuario ID {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al actualizar usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Elimina un usuario del sistema (borrado físico).
     *
     * @param id ID del usuario a eliminar
     * @return Confirmación de la eliminación
     */
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            log.info("Eliminando usuario ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Eliminar usuario
            userRepository.delete(user);
            log.info("Usuario ID {} eliminado exitosamente", id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "Usuario eliminado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar eliminar: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * Suspende (banea) a un usuario.
     *
     * @param id ID del usuario a banear
     * @return Resultado de la operación
     */
    @PutMapping("/banUser/{id}")
    public ResponseEntity<?> banUser(@PathVariable Integer id) {
        try {
            log.info("Suspendiendo usuario ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Cambiar estado a "baneado" (0)
            user.setState("0");
            User updatedUser = userRepository.save(user);
            log.info("Usuario ID {} suspendido exitosamente", id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("state", updatedUser.getState());
            response.put("message", "Usuario baneado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar suspender: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al suspender usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al banear usuario: " + e.getMessage());
        }
    }

    /**
     * Restaura (desbanea) a un usuario suspendido.
     *
     * @param id ID del usuario a desbanear
     * @return Resultado de la operación
     */
    @PutMapping("/unbanUser/{id}")
    public ResponseEntity<?> unbanUser(@PathVariable Integer id) {
        try {
            log.info("Reactivando usuario ID: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Cambiar estado a "activo" (1)
            user.setState("1");
            User updatedUser = userRepository.save(user);
            log.info("Usuario ID {} reactivado exitosamente", id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("state", updatedUser.getState());
            response.put("message", "Usuario desbaneado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar reactivar: ID {}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al reactivar usuario ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al desbanear usuario: " + e.getMessage());
        }
    }

    /**
     * Obtiene usuarios filtrados por rol.
     *
     * @param roleId ID del rol para filtrar
     * @return Lista de usuarios con el rol especificado
     */
    @GetMapping("/getUsersByRole/{roleId}")
    public ResponseEntity<?> getUsersByRole(@PathVariable Integer roleId) {
        try {
            log.info("Obteniendo usuarios con rol ID: {}", roleId);
            // Verificar si el rol existe
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NoSuchElementException("Rol no encontrado con ID: " + roleId));

            // Buscar usuarios por rol
            List<User> users = userRepository.findByRole(role);

            if (users.isEmpty()) {
                log.warn("No se encontraron usuarios con rol ID: {}", roleId);
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .header("X-Info", "No se encontraron usuarios con el rol especificado")
                        .build();
            }

            // Preparar respuesta
            ArrayList<LinkedHashMap<String, Object>> listaContent = new ArrayList<>();
            for (User user : users) {
                LinkedHashMap<String, Object> userContent = new LinkedHashMap<>();
                userContent.put("id", user.getId());
                userContent.put("name", user.getName());
                userContent.put("lastname", user.getLastname());
                userContent.put("username", user.getUsername());
                userContent.put("role", user.getRole().getName());
                userContent.put("code", user.getCode());
                userContent.put("state", user.getState());
                listaContent.add(userContent);
            }

            log.debug("Se encontraron {} usuarios con rol ID: {}", users.size(), roleId);
            return ResponseEntity.ok(listaContent);
        } catch (NoSuchElementException e) {
            log.warn("Rol no encontrado: ID {}", roleId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Rol no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al obtener usuarios por rol ID {}: {}", roleId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuarios por rol: " + e.getMessage());
        }
    }



}
