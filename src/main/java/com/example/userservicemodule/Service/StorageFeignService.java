package com.example.userservicemodule.Service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name="StorageServiceModule")
public interface StorageFeignService {

    /**
     * Obtiene la lista de todos los archivos almacenados
     * @return Objeto genérico con la respuesta
     */
    @GetMapping("/api/files")
    Object listFiles();

    /**
     * Sube un archivo al servidor
     * @param file Archivo a subir
     * @return Objeto genérico con la respuesta
     */
    @PostMapping(value = "/api/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object uploadFile(@RequestPart("file") MultipartFile file);

    /**
     * Obtiene un archivo específico por su nombre
     * @param filename Nombre del archivo a obtener
     * @return Objeto genérico con la respuesta
     */
    @GetMapping("/api/files/{filename:.+}")
    ResponseEntity<byte[]> getFile(@PathVariable("filename") String filename);

    /**
     * Elimina un archivo específico
     * @param filename Nombre del archivo a eliminar
     * @return Objeto genérico con la respuesta
     */
    @DeleteMapping("/api/files/{filename:.+}")
    Object deleteFile(@PathVariable("filename") String filename);
}
