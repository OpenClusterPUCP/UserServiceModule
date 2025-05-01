package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Beans.ErrorResponse;
import com.example.userservicemodule.BeansRequest.FlavorRequest;
import com.example.userservicemodule.Entity.Flavor;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Entity.VirtualMachine;
import com.example.userservicemodule.Repository.FlavorRepository;
import com.example.userservicemodule.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/Admin")

public class AdminFlavorController {
    @Autowired
    FlavorRepository flavorRepository;
    @Autowired
    UserRepository userRepository;

    //METODOS PARA FLAVORS
    @GetMapping("/flavors/list/{userId}")
    public ResponseEntity<?> getUserFlavors(@PathVariable Integer userId) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        // Validar que el ID no sea nulo
        if (userId == null) {
            headers.add("X-Error-Type", "VALIDATION_ERROR");
            headers.add("X-Error-Code", "USER_ID_NULL");

            log.error("Validation error: User ID cannot be null");

            return ResponseEntity
                    .badRequest()
                    .headers(headers)
                    .body(new ErrorResponse("User ID cannot be null"));
        }

        // Verificar si el usuario existe
        if (!userRepository.existsById(userId)) {
            headers.add("X-Error-Type", "RESOURCE_ERROR");
            headers.add("X-Error-Code", "USER_NOT_FOUND");

            log.error("Resource error: User not found with ID: {}", userId);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .headers(headers)
                    .body(new ErrorResponse("User not found with ID: " + userId));
        }

        try {
            // Obtener los flavors del usuario
            List<Flavor> flavors = flavorRepository.findFlavorsByUserId(userId);
            LinkedHashMap<String , Object > json = new LinkedHashMap<>();
            ArrayList<LinkedHashMap<String , Object >> content =  new ArrayList<>();
            for(Flavor f : flavors){
                if(f.getState().equals("active")){
                    LinkedHashMap<String , Object > jsonContent = new LinkedHashMap<>();
                    jsonContent.put("idFlavor" , f.getId());
                    jsonContent.put("name" , f.getName());
                    jsonContent.put("ram" , f.getRam());
                    jsonContent.put("vcpu" , f.getVcpu());
                    jsonContent.put("disk" , f.getDisk());
                    jsonContent.put("type" , f.getType());
                    Integer count = 0 ;
                    for(VirtualMachine vm  : f.getVirtualMachines()){
                        if(vm.getStatus().equals("running")){
                           count =  count +1 ;
                        }
                    }
                    jsonContent.put("state" ,! (count>0)  );
                    content.add(jsonContent);
                }
            }

            json.put("content" ,  content);
            // Si no hay flavors, retornar una lista vacía con código 200 OK
            if (flavors.isEmpty()) {
                log.info("No flavors found for user ID: {}", userId);

                headers.add("X-Result-Count", "0");

                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(Collections.emptyList());
            }

            // Retornar los flavors encontrados
            headers.add("X-Result-Count", String.valueOf(flavors.size()));
            log.info("Retrieved {} flavors for user ID: {}", flavors.size(), userId);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            log.error("Error retrieving flavors for user ID: {}", userId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error retrieving flavors: " + e.getMessage()));
        }
    }

    @PostMapping("/flavors/create")
    public ResponseEntity<?> createFlavor(@RequestBody FlavorRequest flavorRequest) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar los datos de entrada básicos
            if (flavorRequest.getName() == null || flavorRequest.getName().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_NAME_REQUIRED");

                log.error("Validation error: Flavor name is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor name is required"));
            }

            if (flavorRequest.getRam() == null || flavorRequest.getRam() <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_RAM");

                log.error("Validation error: RAM must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("RAM must be a positive number"));
            }

            if (flavorRequest.getVcpu() == null || flavorRequest.getVcpu() <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_VCPU");

                log.error("Validation error: VCPU must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("VCPU must be a positive number"));
            }

            if (flavorRequest.getDisk() == null || flavorRequest.getDisk().compareTo(BigDecimal.ZERO) <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_DISK");

                log.error("Validation error: Disk must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Disk must be a positive number"));
            }

            // Validar que el campo type esté presente y sea "public" o "private"
            if (flavorRequest.getType() == null || flavorRequest.getType().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_TYPE_REQUIRED");

                log.error("Validation error: Flavor type is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor type is required"));
            }

            // Validar que el tipo sea "public" o "private"
            String type = flavorRequest.getType().trim().toLowerCase();
            if (!type.equals("public") && !type.equals("private")) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_FLAVOR_TYPE");

                log.error("Validation error: Invalid flavor type: {}. Must be 'public' or 'private'", flavorRequest.getType());

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Invalid flavor type. Must be 'public' or 'private'"));
            }

            // Validar la consistencia de los datos según sea public o private
            boolean isPublic = "public".equals(type);

            if (!isPublic && flavorRequest.getUserId() == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "USER_ID_REQUIRED_FOR_PRIVATE");

                log.error("Validation error: userId is required for private flavors");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("For private flavors, userId is required"));
            }

            if (isPublic && flavorRequest.getUserId() != null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "USER_ID_NOT_ALLOWED_FOR_PUBLIC");

                log.error("Validation error: userId should not be provided for public flavors");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("For public flavors, userId should not be provided"));
            }

            // Crear un nuevo objeto Flavor
            Flavor flavor = new Flavor();
            flavor.setName(flavorRequest.getName());
            flavor.setRam(flavorRequest.getRam());
            flavor.setVcpu(flavorRequest.getVcpu());
            flavor.setDisk(flavorRequest.getDisk());
            flavor.setType(flavorRequest.getType().toLowerCase().trim()); // Guardar el tipo en minúsculas
            flavor.setState("active");
            // Asignar usuario según el tipo de flavor
            if (!isPublic) {
                // Buscar el usuario por ID
                User user = userRepository.findById(flavorRequest.getUserId())
                        .orElse(null);

                // Si el usuario no existe, devolver un error
                if (user == null) {
                    headers.add("X-Error-Type", "RESOURCE_ERROR");
                    headers.add("X-Error-Code", "USER_NOT_FOUND");

                    log.error("Resource error: User not found with ID: {}", flavorRequest.getUserId());

                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .headers(headers)
                            .body(new ErrorResponse("User not found with ID: " + flavorRequest.getUserId()));
                }

                // Asignar el usuario al flavor
                flavor.setUser(user);
                log.info("Creating private flavor assigned to user ID: {}", user.getId());
            } else {
                // Es un flavor público (sin usuario asignado)
                flavor.setUser(null);
                log.info("Creating public flavor (no user assigned)");
            }

            // Guardar el flavor en la base de datos
            Flavor savedFlavor = flavorRepository.save(flavor);

            // Crear la respuesta con los datos del flavor creado
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("idFlavor", savedFlavor.getId());
            content.put("name", savedFlavor.getName());
            content.put("ram", savedFlavor.getRam());
            content.put("vcpu", savedFlavor.getVcpu());
            content.put("disk", savedFlavor.getDisk());
            content.put("type", savedFlavor.getType());

            if (savedFlavor.getUser() != null) {
                content.put("userId", savedFlavor.getUser().getId());
            }

            json.put("content", content);

            // Agregar cabeceras de respuesta
            headers.add("X-Resource-Id", String.valueOf(savedFlavor.getId()));
            headers.add("X-Resource-Type", "flavor");
            headers.add("X-Flavor-Visibility", savedFlavor.getType().toUpperCase());

            log.info("Successfully created {} flavor with ID: {}",
                    savedFlavor.getType(),
                    savedFlavor.getId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            log.error("Error creating flavor: {}", e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error creating flavor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/flavors/delete/{flavorId}")
    public ResponseEntity<?> deleteFlavor(@PathVariable Integer flavorId) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar que el ID no sea nulo
            if (flavorId == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_ID_NULL");

                log.error("Validation error: Flavor ID cannot be null");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor ID cannot be null"));
            }

            // Buscar el flavor por ID
            Optional<Flavor> flavorOptional = flavorRepository.findById(flavorId);

            // Verificar si el flavor existe
            if (!flavorOptional.isPresent()) {
                headers.add("X-Error-Type", "RESOURCE_ERROR");
                headers.add("X-Error-Code", "FLAVOR_NOT_FOUND");

                log.error("Resource error: Flavor not found with ID: {}", flavorId);

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(new ErrorResponse("Flavor not found with ID: " + flavorId));
            }

            Flavor flavor = flavorOptional.get();

            // Verificar si el flavor está siendo utilizado por alguna máquina virtual
            if (flavor.getVirtualMachines() != null && !flavor.getVirtualMachines().isEmpty()) {
                headers.add("X-Error-Type", "BUSINESS_ERROR");
                headers.add("X-Error-Code", "FLAVOR_IN_USE");
                headers.add("X-VMs-Count", String.valueOf(flavor.getVirtualMachines().size()));

                log.error("Business error: Cannot delete flavor with ID: {} because it is being used by {} virtual machines",
                        flavorId, flavor.getVirtualMachines().size());

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .headers(headers)
                        .body(new ErrorResponse("Cannot delete flavor because it is being used by " +
                                flavor.getVirtualMachines().size() + " virtual machine(s)"));
            }

            // Si el flavor no está siendo utilizado, proceder con la eliminación
            flavor.setState("inactive");
            flavorRepository.save(flavor);

            log.info("Successfully deleted flavor with ID: {}", flavorId);

            headers.add("X-Resource-Id", String.valueOf(flavorId));
            headers.add("X-Resource-Type", "flavor");
            headers.add("X-Operation-Result", "DELETED");

            // Crear una respuesta JSON con información sobre la operación
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("message", "Flavor successfully deleted");
            content.put("flavorId", flavorId);
            content.put("flavorName", flavor.getName());
            content.put("flavorType", flavor.getType());

            json.put("content", content);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            log.error("Error deleting flavor with ID: {}", flavorId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error deleting flavor: " + e.getMessage()));
        }
    }

    @PostMapping("/flavors/update/{flavorId}")
    public ResponseEntity<?> updateFlavor(@PathVariable Integer flavorId, @RequestBody FlavorRequest flavorRequest,
                                          @RequestParam(required = true , value= "idAdmin") Integer adminUserId) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar que el ID no sea nulo
            if (flavorId == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_ID_NULL");

                log.error("Validation error: Flavor ID cannot be null");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor ID cannot be null"));
            }

            // Validar que el ID de administrador no sea nulo
            if (adminUserId == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "ADMIN_ID_NULL");

                log.error("Validation error: Admin user ID cannot be null");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Admin user ID cannot be null"));
            }

            // Buscar el admin por ID
            User admin = userRepository.findById(adminUserId)
                    .orElse(null);

            // Verificar si el administrador existe
            if (admin == null) {
                headers.add("X-Error-Type", "RESOURCE_ERROR");
                headers.add("X-Error-Code", "ADMIN_NOT_FOUND");

                log.error("Resource error: Admin user not found with ID: {}", adminUserId);

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(new ErrorResponse("Admin user not found with ID: " + adminUserId));
            }

            // Buscar el flavor por ID
            Optional<Flavor> flavorOptional = flavorRepository.findById(flavorId);

            // Verificar si el flavor existe
            if (!flavorOptional.isPresent()) {
                headers.add("X-Error-Type", "RESOURCE_ERROR");
                headers.add("X-Error-Code", "FLAVOR_NOT_FOUND");

                log.error("Resource error: Flavor not found with ID: {}", flavorId);

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(new ErrorResponse("Flavor not found with ID: " + flavorId));
            }

            Flavor flavor = flavorOptional.get();

            // Verificar si el flavor es privado pero no pertenece al administrador
            if (flavor.getUser() != null && !flavor.getUser().getId().equals(adminUserId)) {
                headers.add("X-Error-Type", "AUTHORIZATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_NOT_OWNED");

                log.error("Authorization error: Admin user ID: {} does not own the private flavor with ID: {}",
                        adminUserId, flavorId);

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .headers(headers)
                        .body(new ErrorResponse("You can only update private flavors that belong to you or public flavors"));
            }

            // Verificar si el flavor está siendo utilizado por alguna máquina virtual
            if (flavor.getVirtualMachines() != null && !flavor.getVirtualMachines().isEmpty()) {
                headers.add("X-Error-Type", "BUSINESS_ERROR");
                headers.add("X-Error-Code", "FLAVOR_IN_USE");
                headers.add("X-VMs-Count", String.valueOf(flavor.getVirtualMachines().size()));

                log.error("Business error: Cannot update flavor with ID: {} because it is being used by {} virtual machines",
                        flavorId, flavor.getVirtualMachines().size());

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .headers(headers)
                        .body(new ErrorResponse("Cannot update flavor because it is being used by " +
                                flavor.getVirtualMachines().size() + " virtual machine(s)"));
            }

            // Validar los datos de entrada básicos
            if (flavorRequest.getName() == null || flavorRequest.getName().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_NAME_REQUIRED");

                log.error("Validation error: Flavor name is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor name is required"));
            }

            if (flavorRequest.getRam() == null || flavorRequest.getRam() <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_RAM");

                log.error("Validation error: RAM must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("RAM must be a positive number"));
            }

            if (flavorRequest.getVcpu() == null || flavorRequest.getVcpu() <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_VCPU");

                log.error("Validation error: VCPU must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("VCPU must be a positive number"));
            }

            if (flavorRequest.getDisk() == null || flavorRequest.getDisk().compareTo(BigDecimal.ZERO) <= 0) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_DISK");

                log.error("Validation error: Disk must be a positive number");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Disk must be a positive number"));
            }

            // Validar que el campo type esté presente y sea "public" o "private"
            if (flavorRequest.getType() == null || flavorRequest.getType().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "FLAVOR_TYPE_REQUIRED");

                log.error("Validation error: Flavor type is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Flavor type is required"));
            }

            // Validar que el tipo sea "public" o "private"
            String type = flavorRequest.getType().trim().toLowerCase();
            if (!type.equals("public") && !type.equals("private")) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_FLAVOR_TYPE");

                log.error("Validation error: Invalid flavor type: {}. Must be 'public' or 'private'", flavorRequest.getType());

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Invalid flavor type. Must be 'public' or 'private'"));
            }

            // Determinar si el flavor será público o privado
            boolean isPublic = "public".equals(type);

            // Actualizar los datos del flavor
            flavor.setName(flavorRequest.getName());
            flavor.setRam(flavorRequest.getRam());
            flavor.setVcpu(flavorRequest.getVcpu());
            flavor.setDisk(flavorRequest.getDisk());
            flavor.setType(flavorRequest.getType().toLowerCase().trim());

            // Si el flavor va a ser privado, asignarlo al usuario que realiza la acción
            // Si va a ser público, quitar cualquier asignación de usuario
            if (!isPublic) {
                // Asignar el flavor al administrador que está realizando la acción
                flavor.setUser(admin);
                log.info("Updating flavor to private, assigned to admin user ID: {}", admin.getId());
            } else {
                // Es un flavor público (sin usuario asignado)
                flavor.setUser(null);
                log.info("Updating flavor to public (no user assigned)");
            }

            // Guardar los cambios en la base de datos
            Flavor updatedFlavor = flavorRepository.save(flavor);

            // Crear la respuesta con los datos del flavor actualizado
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("idFlavor", updatedFlavor.getId());
            content.put("name", updatedFlavor.getName());
            content.put("ram", updatedFlavor.getRam());
            content.put("vcpu", updatedFlavor.getVcpu());
            content.put("disk", updatedFlavor.getDisk());
            content.put("type", updatedFlavor.getType());

            if (updatedFlavor.getUser() != null) {
                content.put("userId", updatedFlavor.getUser().getId());
            }

            json.put("content", content);

            // Agregar cabeceras de respuesta
            headers.add("X-Resource-Id", String.valueOf(updatedFlavor.getId()));
            headers.add("X-Resource-Type", "flavor");
            headers.add("X-Operation-Result", "UPDATED");

            log.info("Successfully updated flavor with ID: {}", updatedFlavor.getId());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            log.error("Error updating flavor with ID: {}", flavorId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error updating flavor: " + e.getMessage()));
        }
    }
}
