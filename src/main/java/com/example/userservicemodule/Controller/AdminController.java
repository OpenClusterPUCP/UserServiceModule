package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Entity.Role;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.RoleRepository;
import com.example.userservicemodule.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Link;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/Admin")
public class AdminController {



    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> getUsersInfo() {
        try {
            // Check if repository is available
            if (userRepository == null) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("X-Error-Type", "RepositoryUnavailable")
                        .body("User repository is not available");
            }

            // Get all users and handle empty result
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
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
                listaContent.add(userContent);
            }

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
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("X-Error-Type", "DatabaseError")
                    .header("X-Error-Message", ex.getMessage())
                    .body("Database access error occurred");
        } catch (Exception ex) {
            // Handle all other unexpected errors
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "UnexpectedError")
                    .header("X-Error-Message", ex.getMessage())
                    .body("An unexpected error occurred");
        }
    }

    /**
     * Crea un nuevo usuario
     */
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        try {
            // Validar datos requeridos
            if (!userData.containsKey("username") || !userData.containsKey("password") ||
                    !userData.containsKey("name") || !userData.containsKey("lastname") ||
                    !userData.containsKey("code") || !userData.containsKey("role")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Datos de usuario incompletos");
            }

            // Verificar si el usuario ya existe
            String username = (String) userData.get("username");
            if (userRepository.findByUsername(username).isPresent()) {
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
            newUser.setPassword(passwordEncoder.encode((String) userData.get("password")));
            newUser.setName((String) userData.get("name"));
            newUser.setLastname((String) userData.get("lastname"));
            newUser.setCode((String) userData.get("code"));
            newUser.setRole(role);
            newUser.setState(userData.containsKey("state") ? (String) userData.get("state") : "1");

            // Guardar el usuario
            User savedUser = userRepository.save(newUser);

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("name", savedUser.getName());
            response.put("lastname", savedUser.getLastname());
            response.put("code", savedUser.getCode());
            response.put("role", savedUser.getRole().getName());
            response.put("state", savedUser.getState());
            response.put("message", "Usuario creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage());
        }
    }


    /**
     * Elimina un usuario (borrado físico)
     */
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + id));

            // Eliminar usuario
            userRepository.delete(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("message", "Usuario eliminado exitosamente");

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * Obtiene información detallada de un usuario específico
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
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

            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener información del usuario: " + e.getMessage());
        }
    }

}
