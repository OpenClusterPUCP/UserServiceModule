package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Beans.ErrorResponse;
import com.example.userservicemodule.Beans.ImageRequest;
import com.example.userservicemodule.Entity.Image;
import com.example.userservicemodule.Entity.User;
import com.example.userservicemodule.Entity.VirtualMachine;
import com.example.userservicemodule.Repository.ImageRepository;
import com.example.userservicemodule.Repository.UserRepository;
import com.example.userservicemodule.Repository.VirtualMachineRepository;
import com.example.userservicemodule.Service.StorageFeignService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/Admin")
public class AdminImageController {
    @Autowired
    private VirtualMachineRepository virtualMachineRepository;
    @Autowired
    private StorageFeignService storageFeignService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;



    @GetMapping("/images/list/{userId}")
    public ResponseEntity<?> getUserImages(@PathVariable Integer userId) {
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
            // Obtener las imágenes del usuario
            List<Image> images = imageRepository.findImagesByUserId(userId);
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            ArrayList<LinkedHashMap<String, Object>> content = new ArrayList<>();

            for (Image img : images) {
                if (img.getState().equals("active")) {
                    LinkedHashMap<String, Object> jsonContent = new LinkedHashMap<>();
                    jsonContent.put("idImage", img.getId());
                    jsonContent.put("name", img.getName());
                    jsonContent.put("path", img.getPath());
                    jsonContent.put("type", img.getType());
                    jsonContent.put("state", img.getState());
                    jsonContent.put("so", img.getSo());
                    jsonContent.put("version", img.getVersion());
                    jsonContent.put("disco", img.getDisco());
                    jsonContent.put("size", img.getSize());
                    jsonContent.put("description" , img.getDescription());
                    Integer count = 0;
                    for(VirtualMachine vm : virtualMachineRepository.findAll()){
                        if(vm.getImage().getId() ==  img.getId()){
                            count =  count + 1 ;
                        }
                    }
                    if(count>0){
                        jsonContent.put("show" , false);
                    }else{
                        jsonContent.put("show" , true);
                    }
                    content.add(jsonContent);
                }
            }

            json.put("content", content);

            // Si no hay imágenes, retornar una lista vacía con código 200 OK
            if (images.isEmpty()) {
                log.info("No images found for user ID: {}", userId);

                headers.add("X-Result-Count", "0");

                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(Collections.emptyList());
            }

            // Retornar las imágenes encontradas
            headers.add("X-Result-Count", String.valueOf(images.size()));
            log.info("Retrieved {} images for user ID: {}", images.size(), userId);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            log.error("Error retrieving images for user ID: {}", userId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error retrieving images: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/images/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<?> createImage(@RequestPart("imageData") ImageRequest imageRequest,
                                         @RequestPart(value = "file", required = false) MultipartFile file) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar los datos de entrada básicos
            if (imageRequest.getName() == null || imageRequest.getName().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_NAME_REQUIRED");

                log.error("Validation error: Image name is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image name is required"));
            }

            // Validar que el campo type esté presente y sea "public" o "private"
            if (imageRequest.getType() == null || imageRequest.getType().trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_TYPE_REQUIRED");

                log.error("Validation error: Image type is required");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image type is required"));
            }

            // Validar que el tipo sea "public" o "private"
            String type = imageRequest.getType().trim().toLowerCase();
            if (!type.equals("public") && !type.equals("private")) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_IMAGE_TYPE");

                log.error("Validation error: Invalid image type: {}. Must be 'public' or 'private'", imageRequest.getType());

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Invalid image type. Must be 'public' or 'private'"));
            }

            // Validar la consistencia de los datos según sea public o private
            boolean isPublic = "public".equals(type);

            if (!isPublic && imageRequest.getUserId() == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "USER_ID_REQUIRED_FOR_PRIVATE");

                log.error("Validation error: userId is required for private images");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("For private images, userId is required"));
            }

            if (isPublic && imageRequest.getUserId() != null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "USER_ID_NOT_ALLOWED_FOR_PUBLIC");

                log.error("Validation error: userId should not be provided for public images");

                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("For public images, userId should not be provided"));
            }

            // Procesar el archivo de imagen si está presente
            String savedFilename = null;
            String fileUrl = null;

            if (file != null && !file.isEmpty()) {
                // Validar tipo de archivo (solo permitir formatos de imagen)
                String contentType = file.getContentType();
                if (contentType == null) {
                    headers.add("X-Error-Type", "VALIDATION_ERROR");
                    headers.add("X-Error-Code", "INVALID_FILE_TYPE");
                    log.error("Validation error: Invalid file type. Only image files are allowed.");
                    return ResponseEntity
                            .badRequest()
                            .headers(headers)
                            .body(new ErrorResponse("Invalid file type. Only image files are allowed."));
                }

                try {
                    // Usar Feign Client para subir el archivo al servicio de almacenamiento
                    Object uploadResponse = createImage(file);

                    // Extraer la información necesaria del resultado (asumiendo estructura LinkedHashMap)
                    if (uploadResponse instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) uploadResponse;
                        savedFilename = (String) responseMap.get("savedFilename");
                        fileUrl = (String) responseMap.get("fileUrl");

                        log.info("File uploaded successfully to storage service. Saved filename: {}", savedFilename);
                    } else {
                        log.warn("Unexpected response format from storage service: {}", uploadResponse);
                        // Manejar respuesta no esperada
                        savedFilename = UUID.randomUUID().toString(); // Fallback
                    }
                } catch (Exception e) {
                    log.error("Error uploading file to storage service", e);
                    headers.add("X-Error-Type", "SERVER_ERROR");
                    headers.add("X-Error-Code", "STORAGE_SERVICE_ERROR");
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .headers(headers)
                            .body(new ErrorResponse("Error uploading file to storage service: " + e.getMessage()));
                }
            }

            // Crear un nuevo objeto Image
            Image image = new Image();
            image.setName(imageRequest.getName());
            image.setPath("images/" + savedFilename); // Guardamos el nombre del archivo generado por el servicio
            image.setType(imageRequest.getType().toLowerCase().trim());
            image.setState("active");
            image.setDisco((new BigDecimal(imageRequest.getDisco().replace(" GB" , ""))));
            image.setDescription(imageRequest.getDescription());
            image.setSo(imageRequest.getOs());
            image.setSize(imageRequest.getImageSize());
            image.setVersion(imageRequest.getVersion());
            // Asignar usuario según el tipo de imagen
            if (!isPublic) {
                // Buscar el usuario por ID
                User user = userRepository.findById(imageRequest.getUserId())
                        .orElse(null);

                if (user == null) {
                    headers.add("X-Error-Type", "RESOURCE_ERROR");
                    headers.add("X-Error-Code", "USER_NOT_FOUND");
                    log.error("Resource error: User not found with ID: {}", imageRequest.getUserId());
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .headers(headers)
                            .body(new ErrorResponse("User not found with ID: " + imageRequest.getUserId()));
                }

                // Asignar el usuario a la imagen
                image.setUser(user);
                log.info("Creating private image assigned to user ID: {}", user.getId());
            } else {
                // Es una imagen pública (sin usuario asignado)
                image.setUser(null);
                log.info("Creating public image (no user assigned)");
            }

            // Guardar la imagen en la base de datos
            Image savedImage = imageRepository.save(image);

            // Crear la respuesta con los datos de la imagen creada
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("idImage", savedImage.getId());
            content.put("name", savedImage.getName());
            content.put("path", savedImage.getPath());
            content.put("type", savedImage.getType());
            content.put("state", savedImage.getState());
            content.put("fileUrl", fileUrl); // Incluir la URL del archivo

            if (savedImage.getUser() != null) {
                content.put("userId", savedImage.getUser().getId());
            }

            json.put("content", content);

            // Agregar cabeceras de respuesta
            headers.add("X-Resource-Id", String.valueOf(savedImage.getId()));
            headers.add("X-Resource-Type", "image");
            headers.add("X-Image-Visibility", savedImage.getType().toUpperCase());

            log.info("Successfully created {} image with ID: {}",
                    savedImage.getType(),
                    savedImage.getId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");
            log.error("Error creating image: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error creating image: " + e.getMessage()));
        }
    }
    @DeleteMapping("/images/delete/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer imageId) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar que el ID no sea nulo
            if (imageId == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_ID_NULL");
                log.error("Validation error: Image ID cannot be null");
                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image ID cannot be null"));
            }

            // Buscar la imagen por ID
            Optional<Image> imageOptional = imageRepository.findById(imageId);

            // Verificar si la imagen existe
            if (!imageOptional.isPresent()) {
                headers.add("X-Error-Type", "RESOURCE_ERROR");
                headers.add("X-Error-Code", "IMAGE_NOT_FOUND");
                log.error("Resource error: Image not found with ID: {}", imageId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(new ErrorResponse("Image not found with ID: " + imageId));
            }

            Image image = imageOptional.get();

            // Eliminar el archivo físico si existe usando el servicio de almacenamiento
            if (image.getPath() != null && !image.getPath().isEmpty()) {
                try {
                    // Usar Feign Client para eliminar el archivo
                    Object deleteResponse = storageFeignService.deleteFile(image.getPath());
                    log.info("File deleted from storage service: {}", image.getPath());
                } catch (Exception e) {
                    // Solo registrar el error, pero continuar con la eliminación de la referencia
                    log.warn("Error deleting file from storage service: {}", image.getPath(), e);
                }
            }

            // Eliminar la imagen de la base de datos
            image.setState("inactive");
            imageRepository.save(image);

            log.info("Successfully deleted image with ID: {}", imageId);

            headers.add("X-Resource-Id", String.valueOf(imageId));
            headers.add("X-Resource-Type", "image");
            headers.add("X-Operation-Result", "DELETED");

            // Crear una respuesta JSON con información sobre la operación
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("message", "Image successfully deleted");
            content.put("imageId", imageId);
            content.put("imageName", image.getName());
            content.put("imageType", image.getType());

            json.put("content", content);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");
            log.error("Error deleting image with ID: {}", imageId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error deleting image: " + e.getMessage()));
        }
    }
    @PostMapping(value= "/images/update/{imageId}" , consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseEntity<?> updateImage(@PathVariable Integer imageId,
                                         @RequestParam String name,
                                         @RequestParam(required = false) String description,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = true, value = "idAdmin") Integer adminUserId) {
        // Crear un objeto para las cabeceras HTTP personalizadas
        HttpHeaders headers = new HttpHeaders();

        try {
            // Validar que el ID no sea nulo
            if (imageId == null) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_ID_NULL");
                log.error("Validation error: Image ID cannot be null");
                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image ID cannot be null"));
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

            // Buscar la imagen por ID
            Optional<Image> imageOptional = imageRepository.findById(imageId);

            // Verificar si la imagen existe
            if (!imageOptional.isPresent()) {
                headers.add("X-Error-Type", "RESOURCE_ERROR");
                headers.add("X-Error-Code", "IMAGE_NOT_FOUND");
                log.error("Resource error: Image not found with ID: {}", imageId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .headers(headers)
                        .body(new ErrorResponse("Image not found with ID: " + imageId));
            }

            Image image = imageOptional.get();

            // Verificar si la imagen es privada pero no pertenece al administrador
            if (image.getUser() != null && !image.getUser().getId().equals(adminUserId)) {
                headers.add("X-Error-Type", "AUTHORIZATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_NOT_OWNED");
                log.error("Authorization error: Admin user ID: {} does not own the private image with ID: {}",
                        adminUserId, imageId);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .headers(headers)
                        .body(new ErrorResponse("You can only update private images that belong to you or public images"));
            }

            // Validar los datos de entrada básicos
            if (name == null || name.trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "IMAGE_NAME_REQUIRED");
                log.error("Validation error: Image name is required");
                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image name is required"));
            }

            // Validar que el estado no sea vacío si está presente
            if (state != null && state.trim().isEmpty()) {
                headers.add("X-Error-Type", "VALIDATION_ERROR");
                headers.add("X-Error-Code", "INVALID_IMAGE_STATE");
                log.error("Validation error: Image state cannot be empty if provided");
                return ResponseEntity
                        .badRequest()
                        .headers(headers)
                        .body(new ErrorResponse("Image state cannot be empty if provided"));
            }

            // Actualizar únicamente los campos solicitados: nombre, descripción y estado
            image.setName(name);

            // Actualizar la descripción si está presente
            if (description != null) {
                image.setDescription(description);
            }

            // Actualizar el estado si está presente
            if (state != null) {
                image.setState(state);
            }

            // Guardar los cambios en la base de datos
            Image updatedImage = imageRepository.save(image);

            // Crear la respuesta con los datos de la imagen actualizada
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            LinkedHashMap<String, Object> content = new LinkedHashMap<>();

            content.put("idImage", updatedImage.getId());
            content.put("name", updatedImage.getName());
            content.put("description", updatedImage.getDescription());
            content.put("path", updatedImage.getPath());
            content.put("type", updatedImage.getType());
            content.put("state", updatedImage.getState());

            if (updatedImage.getUser() != null) {
                content.put("userId", updatedImage.getUser().getId());
            }

            json.put("content", content);

            // Agregar cabeceras de respuesta
            headers.add("X-Resource-Id", String.valueOf(updatedImage.getId()));
            headers.add("X-Resource-Type", "image");
            headers.add("X-Operation-Result", "UPDATED");

            log.info("Successfully updated image name, description and state with ID: {}", updatedImage.getId());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(json);

        } catch (Exception e) {
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");
            log.error("Error updating image with ID: {}", imageId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error updating image: " + e.getMessage()));
        }
    }


    public Object createImage(MultipartFile file) {
        String url = getServiceUrl("STORAGESERVICEMODULE")+ "/api/files"; // Ajusta esta URL al endpoint correcto
        try {
            String boundary = "----" + System.currentTimeMillis();
            // Configurar la conexión
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            // Configurar timeouts para evitar errores de conexión
            connection.setConnectTimeout(30000);  // 30 segundos para establecer conexión
            connection.setReadTimeout(180000);    // 3 minutos para leer respuesta

            // Streaming en bloques para archivos grandes
            connection.setChunkedStreamingMode(1024 * 1024); // 1MB chunks

            try (OutputStream output = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                if (file != null && !file.isEmpty()) {
                    // IMPORTANTE: Nombre del parámetro debe ser "file" para coincidir con @RequestPart("file")
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                            .append(file.getOriginalFilename()).append("\"\r\n");
                    writer.append("Content-Type: ").append(file.getContentType()).append("\r\n\r\n");
                    writer.flush();

                    // Transmitir el archivo en bloques
                    try (InputStream inputStream = file.getInputStream()) {
                        byte[] buffer = new byte[8192]; // Buffer de 8KB
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                        output.flush();
                    }
                    writer.append("\r\n");

                    // Cerrar el límite multipart
                    writer.append("--").append(boundary).append("--").append("\r\n");
                    writer.flush();
                } else {
                    // Si el archivo es nulo o vacío, envía un error
                    log.error("El archivo proporcionado es nulo o vacío");
                    return Collections.singletonMap("error", "El archivo proporcionado es nulo o vacío");
                }
            }

            // Obtener la respuesta
            int responseCode = connection.getResponseCode();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                            StandardCharsets.UTF_8))) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                // Procesar la respuesta
                ObjectMapper mapper = new ObjectMapper();
                if (responseCode >= 200 && responseCode < 300) {
                    log.info("Archivo subido exitosamente: {}", file.getOriginalFilename());
                    return mapper.readValue(responseBody.toString(), Object.class);
                } else {
                    log.error("Error del servidor al subir archivo: código {} - respuesta {}", responseCode, responseBody);
                    try {
                        return mapper.readValue(responseBody.toString(), Map.class);
                    } catch (Exception e) {
                        return Collections.singletonMap("error", "Error al subir archivo: código " + responseCode + " - " + responseBody);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error de E/S al comunicarse con la API: {}", e.getMessage(), e);
            return Collections.singletonMap("error", "Error de comunicación con el servidor: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {}", e.getMessage(), e);
            return Collections.singletonMap("error", "Error inesperado al subir archivo: " + e.getMessage());
        }
    }

    @Autowired
    private EurekaClient eurekaClient;

    /**
     * Obtiene información de la instancia de un servicio registrado en Eureka.
     *
     * @param serviceName Nombre del servicio registrado en Eureka
     * @return Map con información de la instancia con host y puerto, o null si no se encuentra
     *
     * Ejemplo de uso:
     *   Map<String, Object> instance = getServiceInstance("storage-service-module");
     *   String url = "http://" + instance.get("ipAddr") + ":" + instance.get("port") + "/api/files";
     */
    public Map<String, Object> getServiceInstance(String serviceName) {
        try {
            log.debug("Buscando instancia de servicio: {}", serviceName);

            // Obtener aplicación desde Eureka
            Application application = eurekaClient.getApplication(serviceName);

            if (application == null || application.getInstances().isEmpty()) {
                log.error("Servicio {} no encontrado en Eureka", serviceName);
                return null;
            }

            // Filtrar por instancias activas (UP)
            List<InstanceInfo> upInstances = application.getInstances().stream()
                    .filter(instance -> instance.getStatus() == InstanceInfo.InstanceStatus.UP)
                    .toList();

            if (upInstances.isEmpty()) {
                log.error("No hay instancias activas del servicio {}", serviceName);
                return null;
            }

            // Obtener primera instancia disponible
            InstanceInfo instance = upInstances.get(0);

            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("ipAddr", instance.getIPAddr());
            serviceInfo.put("port", instance.getPort());
            serviceInfo.put("hostName", instance.getHostName());
            serviceInfo.put("instanceId", instance.getInstanceId());

            log.debug("Instancia encontrada: {}", serviceInfo);

            return serviceInfo;
        } catch (Exception e) {
            log.error("Error obteniendo instancia de {}: {}", serviceName, e.getMessage(), e);
            return null;
        }
    }

    public String getServiceUrl(String serviceName) {
        Map<String, Object> instance = getServiceInstance(serviceName);

        if (instance == null) {
            return null;
        }

        return String.format("http://%s:%s",
                instance.get("ipAddr"),
                instance.get("port"));
    }
}
