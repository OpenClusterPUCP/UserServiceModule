package com.example.userservicemodule.Controller;


import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST para operaciones de usuarios regulares.
 * Proporciona endpoints para que los usuarios gestionen su perfil y accedan a información básica.
 */
@RestController
@RequestMapping("/User")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene información básica de los usuarios del sistema.
     * Para usuarios regulares, se muestra información limitada.
     *
     * @return Lista de usuarios con información básica
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        try {
            log.info("Solicitando lista básica de usuarios (vista usuario regular)");
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                log.warn("No se encontraron usuarios en la base de datos");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build();
            }

            // Para usuarios regulares, solo mostramos información básica
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getName());
                userMap.put("lastname", user.getLastname());
                userMap.put("role", user.getRole().getName());

                // No incluimos información sensible como username, estado, etc.
                responseList.add(userMap);
            }

            log.debug("Se recuperaron {} usuarios (vista limitada)", users.size());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            log.error("Error al obtener lista de usuarios: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuarios: " + e.getMessage());
        }
    }


}