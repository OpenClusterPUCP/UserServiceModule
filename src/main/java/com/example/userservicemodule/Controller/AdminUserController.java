package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Entity.Resource;
import com.example.userservicemodule.Entity.Role;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.FlavorRepository;
import com.example.userservicemodule.Repository.ResourceRepository;
import com.example.userservicemodule.Repository.RoleRepository;
import com.example.userservicemodule.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para operaciones de administración de usuarios.
 * Proporciona endpoints para la gestión completa de usuarios en el sistema.
 */
@RestController
@RequestMapping("/Admin")
@Slf4j
public class AdminUserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceRepository resourceRepository;

    public AdminUserController(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceRepository = resourceRepository;
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
                // Manejar lastLogin que puede ser null
                if (user.getLastLogin() != null) {
                    userContent.put("lastLogin", user.getLastLogin().format(formatter));
                } else {
                    userContent.put("lastLogin", null);
                }

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
            // Manejar lastLogin que puede ser null
            if (user.getLastLogin() != null) {
                response.put("lastLogin", user.getLastLogin().format(formatter));
            } else {
                response.put("lastLogin", null);
            }


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
     * Crea un nuevo usuario en el sistema con recursos iniciales.
     *
     * @param userData Datos del nuevo usuario
     * @return Datos del usuario creado con sus recursos
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
            newUser.setState("1"); // Siempre activo (1)
            newUser.setCreatedAt(LocalDateTime.now());

            // Guardar el usuario
            User savedUser = userRepository.save(newUser);
            log.info("Usuario creado exitosamente: ID {} - {}", savedUser.getId(), savedUser.getUsername());

            // Inicializar recursos por defecto
            Resource resource = new Resource();
            resource.setUser(savedUser);

            // Extraer recursos si vienen en la petición
            Map<String, Object> resourceData = userData.containsKey("resources") ?
                    (Map<String, Object>) userData.get("resources") : null;

            // Asignar valores de recursos (por defecto o especificados)
            if (resourceData != null) {
                resource.setCpu(resourceData.containsKey("cpu") ?
                        (Integer) resourceData.get("cpu") : 4);
                resource.setRam(resourceData.containsKey("ram") ?
                        (Integer) resourceData.get("ram") : 4096);
                resource.setDisk(resourceData.containsKey("disk") ?
                        (Integer) resourceData.get("disk") : 30);
                resource.setSlices(resourceData.containsKey("slices") ?
                        (Integer) resourceData.get("slices") : 1);
            } else {
                // Valores por defecto
                resource.setCpu(4);     // 4 núcleos
                resource.setRam(4096);  // 4 GB de RAM
                resource.setDisk(30);   // 30 GB de disco
                resource.setSlices(1);  // 1 slice
            }

            // Inicializar valores de uso en 0
            resource.setUsedCpu(0);
            resource.setUsedRam(0);
            resource.setUsedDisk(0);
            resource.setUsedSlices(0);

            // Guardar recursos
            Resource savedResource = resourceRepository.save(resource);
            log.info("Recursos inicializados para nuevo usuario ID {}: CPU={}, RAM={}, Disk={}, Slices={}",
                    savedUser.getId(), savedResource.getCpu(), savedResource.getRam(),
                    savedResource.getDisk(), savedResource.getSlices());

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

            // Agregar información de recursos
            Map<String, Object> resourceInfo = new HashMap<>();
            resourceInfo.put("id", savedResource.getId());
            resourceInfo.put("cpu", savedResource.getCpu());
            resourceInfo.put("ram", savedResource.getRam());
            resourceInfo.put("disk", savedResource.getDisk());
            resourceInfo.put("slices", savedResource.getSlices());
            resourceInfo.put("usedCpu", 0);
            resourceInfo.put("usedRam", 0);
            resourceInfo.put("usedDisk", 0);
            resourceInfo.put("usedSlices", 0);
            response.put("resources", resourceInfo);

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


    /**
     * Obtiene los recursos asignados a un usuario.
     *
     * @param userId ID del usuario
     * @return Recursos del usuario
     */
    @GetMapping("/user/{userId}/resources")
    public ResponseEntity<?> getUserResources(@PathVariable Integer userId) {
        try {
            log.info("Solicitando recursos del usuario ID: {}", userId);

            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Buscar recursos del usuario
            Optional<Resource> resourceOpt = resourceRepository.findByUserId(userId);

            if (!resourceOpt.isPresent()) {
                log.warn("No se encontraron recursos para el usuario ID: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("No se encontraron recursos asignados para este usuario");
            }

            Resource resource = resourceOpt.get();

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", resource.getId());
            response.put("userId", userId);
            response.put("username", user.getUsername());

            // Recursos asignados
            response.put("cpu", resource.getCpu());
            response.put("ram", resource.getRam());
            response.put("disk", resource.getDisk());
            response.put("slices", resource.getSlices());

            // Recursos utilizados
            response.put("usedCpu", resource.getUsedCpu() != null ? resource.getUsedCpu() : 0);
            response.put("usedRam", resource.getUsedRam() != null ? resource.getUsedRam() : 0);
            response.put("usedDisk", resource.getUsedDisk() != null ? resource.getUsedDisk() : 0);
            response.put("usedSlices", resource.getUsedSlices() != null ? resource.getUsedSlices() : 0);

            // Calcular porcentajes de uso
            int cpuUsagePercent = resource.getCpu() > 0 && resource.getUsedCpu() != null ?
                    (resource.getUsedCpu() * 100) / resource.getCpu() : 0;
            int ramUsagePercent = resource.getRam() > 0 && resource.getUsedRam() != null ?
                    (resource.getUsedRam() * 100) / resource.getRam() : 0;
            int diskUsagePercent = resource.getDisk() > 0 && resource.getUsedDisk() != null ?
                    (resource.getUsedDisk() * 100) / resource.getDisk() : 0;
            int slicesUsagePercent = resource.getSlices() > 0 && resource.getUsedSlices() != null ?
                    (resource.getUsedSlices() * 100) / resource.getSlices() : 0;

            response.put("cpuUsagePercent", cpuUsagePercent);
            response.put("ramUsagePercent", ramUsagePercent);
            response.put("diskUsagePercent", diskUsagePercent);
            response.put("slicesUsagePercent", slicesUsagePercent);

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado: ID {}", userId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al obtener recursos del usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener recursos del usuario: " + e.getMessage());
        }
    }

    /**
     * Asigna o actualiza los recursos de un usuario.
     *
     * @param userId ID del usuario
     * @param resourceData Datos de recursos a asignar
     * @return Resultado de la operación
     */
    @PutMapping("/user/{userId}/resources")
    public ResponseEntity<?> updateUserResources(@PathVariable Integer userId,
                                                 @RequestBody Map<String, Object> resourceData) {
        try {
            log.info("Actualizando recursos del usuario ID: {}", userId);

            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Validar datos de entrada
            if (!resourceData.containsKey("cpu") || !resourceData.containsKey("ram") ||
                    !resourceData.containsKey("disk") || !resourceData.containsKey("slices")) {
                log.warn("Datos de recursos incompletos para usuario ID: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datos de recursos incompletos. Se requieren cpu, ram, disk y slices");
            }

            // Intentar encontrar recursos existentes o crear nuevos
            Resource resource = resourceRepository.findByUserId(userId)
                    .orElse(new Resource());

            // Si es un nuevo recurso, asignar el usuario
            if (resource.getId() == null) {
                resource.setUser(user);
                // Inicializar valores de uso en 0
                resource.setUsedCpu(0);
                resource.setUsedRam(0);
                resource.setUsedDisk(0);
                resource.setUsedSlices(0);
            }

            // Actualizar valores de límites
            resource.setCpu((Integer) resourceData.get("cpu"));
            resource.setRam((Integer) resourceData.get("ram"));
            resource.setDisk((Integer) resourceData.get("disk"));
            resource.setSlices((Integer) resourceData.get("slices"));

            // Actualizar valores de uso si se proporcionan
            if (resourceData.containsKey("usedCpu")) {
                resource.setUsedCpu((Integer) resourceData.get("usedCpu"));
            }
            if (resourceData.containsKey("usedRam")) {
                resource.setUsedRam((Integer) resourceData.get("usedRam"));
            }
            if (resourceData.containsKey("usedDisk")) {
                resource.setUsedDisk((Integer) resourceData.get("usedDisk"));
            }
            if (resourceData.containsKey("usedSlices")) {
                resource.setUsedSlices((Integer) resourceData.get("usedSlices"));
            }

            // Guardar cambios
            Resource savedResource = resourceRepository.save(resource);
            log.info("Recursos actualizados para usuario ID {}: CPU={}/{}(usado), RAM={}/{}(usado), Disk={}/{}(usado), Slices={}/{}(usado)",
                    userId, savedResource.getCpu(), savedResource.getUsedCpu(),
                    savedResource.getRam(), savedResource.getUsedRam(),
                    savedResource.getDisk(), savedResource.getUsedDisk(),
                    savedResource.getSlices(), savedResource.getUsedSlices());

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedResource.getId());
            response.put("userId", userId);
            response.put("username", user.getUsername());

            // Recursos asignados
            response.put("cpu", savedResource.getCpu());
            response.put("ram", savedResource.getRam());
            response.put("disk", savedResource.getDisk());
            response.put("slices", savedResource.getSlices());

            // Recursos utilizados
            response.put("usedCpu", savedResource.getUsedCpu() != null ? savedResource.getUsedCpu() : 0);
            response.put("usedRam", savedResource.getUsedRam() != null ? savedResource.getUsedRam() : 0);
            response.put("usedDisk", savedResource.getUsedDisk() != null ? savedResource.getUsedDisk() : 0);
            response.put("usedSlices", savedResource.getUsedSlices() != null ? savedResource.getUsedSlices() : 0);

            // Calcular porcentajes de uso
            int cpuUsagePercent = savedResource.getCpu() > 0 && savedResource.getUsedCpu() != null ?
                    (savedResource.getUsedCpu() * 100) / savedResource.getCpu() : 0;
            int ramUsagePercent = savedResource.getRam() > 0 && savedResource.getUsedRam() != null ?
                    (savedResource.getUsedRam() * 100) / savedResource.getRam() : 0;
            int diskUsagePercent = savedResource.getDisk() > 0 && savedResource.getUsedDisk() != null ?
                    (savedResource.getUsedDisk() * 100) / savedResource.getDisk() : 0;
            int slicesUsagePercent = savedResource.getSlices() > 0 && savedResource.getUsedSlices() != null ?
                    (savedResource.getUsedSlices() * 100) / savedResource.getSlices() : 0;

            response.put("cpuUsagePercent", cpuUsagePercent);
            response.put("ramUsagePercent", ramUsagePercent);
            response.put("diskUsagePercent", diskUsagePercent);
            response.put("slicesUsagePercent", slicesUsagePercent);

            response.put("message", "Recursos actualizados exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar actualizar recursos: ID {}", userId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al actualizar recursos del usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar recursos del usuario: " + e.getMessage());
        }
    }

    /**
     * Actualiza solo el uso de recursos de un usuario (no actualiza los límites).
     * Este método es útil para que otros servicios reporten el uso actual.
     *
     * @param userId ID del usuario
     * @param usageData Datos de uso de recursos
     * @return Resultado de la operación
     */
    @PutMapping("/user/{userId}/resources/usage")
    public ResponseEntity<?> updateResourceUsage(@PathVariable Integer userId,
                                                 @RequestBody Map<String, Object> usageData) {
        try {
            log.info("Actualizando uso de recursos del usuario ID: {}", userId);

            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Verificar si los recursos existen
            Resource resource = resourceRepository.findByUserId(userId)
                    .orElseThrow(() -> new NoSuchElementException("No hay recursos asignados para el usuario con ID: " + userId));

            // Validar datos de entrada (al menos uno debe estar presente)
            if (!usageData.containsKey("usedCpu") && !usageData.containsKey("usedRam") &&
                    !usageData.containsKey("usedDisk") && !usageData.containsKey("usedSlices")) {
                log.warn("Datos de uso de recursos incompletos para usuario ID: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datos de uso de recursos incompletos. Se requiere al menos uno de: usedCpu, usedRam, usedDisk, usedSlices");
            }

            // Actualizar valores de uso
            boolean updated = false;

            if (usageData.containsKey("usedCpu")) {
                resource.setUsedCpu((Integer) usageData.get("usedCpu"));
                updated = true;
            }
            if (usageData.containsKey("usedRam")) {
                resource.setUsedRam((Integer) usageData.get("usedRam"));
                updated = true;
            }
            if (usageData.containsKey("usedDisk")) {
                resource.setUsedDisk((Integer) usageData.get("usedDisk"));
                updated = true;
            }
            if (usageData.containsKey("usedSlices")) {
                resource.setUsedSlices((Integer) usageData.get("usedSlices"));
                updated = true;
            }

            if (!updated) {
                log.warn("No se proporcionaron datos válidos para actualizar el uso de recursos del usuario ID: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("No se proporcionaron datos válidos para actualizar el uso de recursos");
            }

            // Guardar cambios
            Resource savedResource = resourceRepository.save(resource);
            log.info("Uso de recursos actualizado para usuario ID {}: CPU usado={}, RAM usada={}, Disco usado={}, Slices usados={}",
                    userId, savedResource.getUsedCpu(), savedResource.getUsedRam(),
                    savedResource.getUsedDisk(), savedResource.getUsedSlices());

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedResource.getId());
            response.put("userId", userId);
            response.put("username", user.getUsername());

            // Recursos asignados (límites)
            response.put("cpu", savedResource.getCpu());
            response.put("ram", savedResource.getRam());
            response.put("disk", savedResource.getDisk());
            response.put("slices", savedResource.getSlices());

            // Recursos utilizados
            response.put("usedCpu", savedResource.getUsedCpu() != null ? savedResource.getUsedCpu() : 0);
            response.put("usedRam", savedResource.getUsedRam() != null ? savedResource.getUsedRam() : 0);
            response.put("usedDisk", savedResource.getUsedDisk() != null ? savedResource.getUsedDisk() : 0);
            response.put("usedSlices", savedResource.getUsedSlices() != null ? savedResource.getUsedSlices() : 0);

            // Calcular porcentajes de uso
            int cpuUsagePercent = savedResource.getCpu() > 0 && savedResource.getUsedCpu() != null ?
                    (savedResource.getUsedCpu() * 100) / savedResource.getCpu() : 0;
            int ramUsagePercent = savedResource.getRam() > 0 && savedResource.getUsedRam() != null ?
                    (savedResource.getUsedRam() * 100) / savedResource.getRam() : 0;
            int diskUsagePercent = savedResource.getDisk() > 0 && savedResource.getUsedDisk() != null ?
                    (savedResource.getUsedDisk() * 100) / savedResource.getDisk() : 0;
            int slicesUsagePercent = savedResource.getSlices() > 0 && savedResource.getUsedSlices() != null ?
                    (savedResource.getUsedSlices() * 100) / savedResource.getSlices() : 0;

            response.put("cpuUsagePercent", cpuUsagePercent);
            response.put("ramUsagePercent", ramUsagePercent);
            response.put("diskUsagePercent", diskUsagePercent);
            response.put("slicesUsagePercent", slicesUsagePercent);

            response.put("message", "Uso de recursos actualizado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Error al actualizar uso de recursos: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al actualizar uso de recursos del usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar uso de recursos: " + e.getMessage());
        }
    }

    /**
     * Asigna recursos iniciales a un usuario recién creado.
     * Este método se puede llamar automáticamente después de crear un usuario.
     *
     * @param userId ID del usuario nuevo
     * @param defaultResources Recursos por defecto a asignar
     * @return Resultado de la operación
     */
    @PostMapping("/user/{userId}/resources/init")
    public ResponseEntity<?> initializeUserResources(@PathVariable Integer userId,
                                                     @RequestBody(required = false) Map<String, Object> defaultResources) {
        try {
            log.info("Inicializando recursos para el usuario ID: {}", userId);

            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Verificar si ya tiene recursos asignados
            if (resourceRepository.findByUserId(userId).isPresent()) {
                log.warn("El usuario ID {} ya tiene recursos asignados", userId);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("El usuario ya tiene recursos asignados");
            }

            // Crear nuevo recurso con valores por defecto
            Resource resource = new Resource();
            resource.setUser(user);

            // Usar valores por defecto o los proporcionados
            if (defaultResources != null) {
                resource.setCpu(defaultResources.containsKey("cpu") ?
                        (Integer) defaultResources.get("cpu") : 4);
                resource.setRam(defaultResources.containsKey("ram") ?
                        (Integer) defaultResources.get("ram") : 4096);
                resource.setDisk(defaultResources.containsKey("disk") ?
                        (Integer) defaultResources.get("disk") : 30);
                resource.setSlices(defaultResources.containsKey("slices") ?
                        (Integer) defaultResources.get("slices") : 1);
            } else {
                // Valores por defecto si no se proporcionan
                resource.setCpu(4);         // 4 CPU
                resource.setRam(4096);      // 4 GB RAM (en MB)
                resource.setDisk(30);       // 30 GB de disco
                resource.setSlices(1);      // 1 slice
            }

            // Inicializar valores de uso en 0
            resource.setUsedCpu(0);
            resource.setUsedRam(0);
            resource.setUsedDisk(0);
            resource.setUsedSlices(0);

            // Guardar recursos
            Resource savedResource = resourceRepository.save(resource);
            log.info("Recursos inicializados para usuario ID {}: CPU={}, RAM={}, Disk={}, Slices={}",
                    userId, savedResource.getCpu(), savedResource.getRam(),
                    savedResource.getDisk(), savedResource.getSlices());

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedResource.getId());
            response.put("userId", userId);
            response.put("username", user.getUsername());
            response.put("cpu", savedResource.getCpu());
            response.put("ram", savedResource.getRam());
            response.put("disk", savedResource.getDisk());
            response.put("slices", savedResource.getSlices());
            response.put("usedCpu", 0);
            response.put("usedRam", 0);
            response.put("usedDisk", 0);
            response.put("usedSlices", 0);
            response.put("cpuUsagePercent", 0);
            response.put("ramUsagePercent", 0);
            response.put("diskUsagePercent", 0);
            response.put("slicesUsagePercent", 0);
            response.put("message", "Recursos inicializados exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoSuchElementException e) {
            log.warn("Usuario no encontrado al intentar inicializar recursos: ID {}", userId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al inicializar recursos del usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al inicializar recursos del usuario: " + e.getMessage());
        }
    }

    /**
     * Obtiene un listado de todos los recursos de usuarios con sus detalles.
     * Útil para monitoreo de asignación y uso de recursos en el sistema.
     *
     * @return Lista de recursos asignados a todos los usuarios
     */
    @GetMapping("/resources")
    public ResponseEntity<?> getAllUserResources() {
        try {
            log.info("Solicitando listado de recursos de todos los usuarios");

            List<Resource> resources = resourceRepository.findAll();

            if (resources.isEmpty()) {
                log.warn("No se encontraron recursos asignados a ningún usuario");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .header("X-Info", "No se encontraron recursos asignados")
                        .build();
            }

            // Preparar respuesta
            List<Map<String, Object>> resourcesList = new ArrayList<>();
            for (Resource resource : resources) {
                Map<String, Object> resourceData = new HashMap<>();
                resourceData.put("id", resource.getId());
                resourceData.put("userId", resource.getUser().getId());
                resourceData.put("username", resource.getUser().getUsername());
                resourceData.put("userCode", resource.getUser().getCode());

                // Recursos asignados
                resourceData.put("cpu", resource.getCpu());
                resourceData.put("ram", resource.getRam());
                resourceData.put("disk", resource.getDisk());
                resourceData.put("slices", resource.getSlices());

                // Recursos utilizados
                resourceData.put("usedCpu", resource.getUsedCpu() != null ? resource.getUsedCpu() : 0);
                resourceData.put("usedRam", resource.getUsedRam() != null ? resource.getUsedRam() : 0);
                resourceData.put("usedDisk", resource.getUsedDisk() != null ? resource.getUsedDisk() : 0);
                resourceData.put("usedSlices", resource.getUsedSlices() != null ? resource.getUsedSlices() : 0);

                // Calcular porcentajes de uso
                int cpuUsagePercent = resource.getCpu() > 0 && resource.getUsedCpu() != null ?
                        (resource.getUsedCpu() * 100) / resource.getCpu() : 0;
                int ramUsagePercent = resource.getRam() > 0 && resource.getUsedRam() != null ?
                        (resource.getUsedRam() * 100) / resource.getRam() : 0;
                int diskUsagePercent = resource.getDisk() > 0 && resource.getUsedDisk() != null ?
                        (resource.getUsedDisk() * 100) / resource.getDisk() : 0;
                int slicesUsagePercent = resource.getSlices() > 0 && resource.getUsedSlices() != null ?
                        (resource.getUsedSlices() * 100) / resource.getSlices() : 0;

                resourceData.put("cpuUsagePercent", cpuUsagePercent);
                resourceData.put("ramUsagePercent", ramUsagePercent);
                resourceData.put("diskUsagePercent", diskUsagePercent);
                resourceData.put("slicesUsagePercent", slicesUsagePercent);

                resourcesList.add(resourceData);
            }

            log.debug("Se recuperaron recursos para {} usuarios", resources.size());
            return ResponseEntity.ok(resourcesList);
        } catch (Exception e) {
            log.error("Error al obtener recursos de los usuarios: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener recursos de los usuarios: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario tiene suficientes recursos disponibles.
     * Útil para validar antes de asignar nuevos recursos o crear nuevos servicios.
     *
     * @param userId ID del usuario
     * @param requiredResources Recursos requeridos para la operación
     * @return Resultado de la verificación
     */
    @PostMapping("/user/{userId}/resources/check")
    public ResponseEntity<?> checkResourceAvailability(@PathVariable Integer userId,
                                                       @RequestBody Map<String, Object> requiredResources) {
        try {
            log.info("Verificando disponibilidad de recursos para usuario ID: {}", userId);

            // Verificar si el usuario existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

            // Verificar si tiene recursos asignados
            Resource resource = resourceRepository.findByUserId(userId)
                    .orElseThrow(() -> new NoSuchElementException("No hay recursos asignados para el usuario con ID: " + userId));

            // Inicializar variables para los recursos requeridos
            Integer requiredCpu = requiredResources.containsKey("cpu") ? (Integer) requiredResources.get("cpu") : 0;
            Integer requiredRam = requiredResources.containsKey("ram") ? (Integer) requiredResources.get("ram") : 0;
            Integer requiredDisk = requiredResources.containsKey("disk") ? (Integer) requiredResources.get("disk") : 0;
            Integer requiredSlices = requiredResources.containsKey("slices") ? (Integer) requiredResources.get("slices") : 0;

            // Calcular recursos disponibles
            Integer availableCpu = resource.getCpu() - (resource.getUsedCpu() != null ? resource.getUsedCpu() : 0);
            Integer availableRam = resource.getRam() - (resource.getUsedRam() != null ? resource.getUsedRam() : 0);
            Integer availableDisk = resource.getDisk() - (resource.getUsedDisk() != null ? resource.getUsedDisk() : 0);
            Integer availableSlices = resource.getSlices() - (resource.getUsedSlices() != null ? resource.getUsedSlices() : 0);

            // Verificar disponibilidad
            boolean isAvailable = true;
            List<String> insufficientResources = new ArrayList<>();

            if (requiredCpu > availableCpu) {
                isAvailable = false;
                insufficientResources.add("CPU");
            }
            if (requiredRam > availableRam) {
                isAvailable = false;
                insufficientResources.add("RAM");
            }
            if (requiredDisk > availableDisk) {
                isAvailable = false;
                insufficientResources.add("Disk");
            }
            if (requiredSlices > availableSlices) {
                isAvailable = false;
                insufficientResources.add("Slices");
            }

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("username", user.getUsername());
            response.put("isAvailable", isAvailable);

            if (!isAvailable) {
                response.put("insufficientResources", insufficientResources);
                response.put("message", "Recursos insuficientes: " + String.join(", ", insufficientResources));
            } else {
                response.put("message", "Recursos disponibles");
            }

            // Incluir detalles de recursos
            Map<String, Object> resourceDetails = new HashMap<>();
            resourceDetails.put("assigned", new HashMap<String, Integer>() {{
                put("cpu", resource.getCpu());
                put("ram", resource.getRam());
                put("disk", resource.getDisk());
                put("slices", resource.getSlices());
            }});

            resourceDetails.put("used", new HashMap<String, Integer>() {{
                put("cpu", resource.getUsedCpu() != null ? resource.getUsedCpu() : 0);
                put("ram", resource.getUsedRam() != null ? resource.getUsedRam() : 0);
                put("disk", resource.getUsedDisk() != null ? resource.getUsedDisk() : 0);
                put("slices", resource.getUsedSlices() != null ? resource.getUsedSlices() : 0);
            }});

            resourceDetails.put("available", new HashMap<String, Integer>() {{
                put("cpu", availableCpu);
                put("ram", availableRam);
                put("disk", availableDisk);
                put("slices", availableSlices);
            }});

            resourceDetails.put("required", new HashMap<String, Integer>() {{
                put("cpu", requiredCpu);
                put("ram", requiredRam);
                put("disk", requiredDisk);
                put("slices", requiredSlices);
            }});

            response.put("resources", resourceDetails);

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("Error al verificar disponibilidad de recursos: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de recursos para usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar disponibilidad de recursos: " + e.getMessage());
        }
    }




}
