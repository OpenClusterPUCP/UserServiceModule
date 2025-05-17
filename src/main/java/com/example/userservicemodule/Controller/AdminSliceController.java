package com.example.userservicemodule.Controller;

import com.example.userservicemodule.Beans.ErrorResponse;
import com.example.userservicemodule.DTO.Slices.SliceSummaryDTO;
import com.example.userservicemodule.Entity.Flavor;
import com.example.userservicemodule.Entity.VirtualMachine;
import com.example.userservicemodule.Repository.SliceRepository;
import com.example.userservicemodule.Service.AvailabilityZoneService;
import com.example.userservicemodule.Service.SliceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/Admin/slices")
public class AdminSliceController {

    @Autowired
    private SliceService sliceService;

    @GetMapping("/listAll")
    public ResponseEntity<?> listarResumenSlices() {
        try {
            List<SliceSummaryDTO> resumen = sliceService.obtenerResumenSlices();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Result-Count", String.valueOf(resumen.size()));

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(resumen);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Error-Type", "SERVER_ERROR");
            headers.add("X-Error-Code", "DATABASE_ERROR");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(new ErrorResponse("Error al listar slices: " + e.getMessage()));
        }
    }




}
