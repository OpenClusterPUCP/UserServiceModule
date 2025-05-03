package com.example.userservicemodule.Controller;

import com.example.userservicemodule.DTO.AvailabilityZoneDTO;
import com.example.userservicemodule.Entity.AvailabilityZone;
import com.example.userservicemodule.Service.AvailabilityZoneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Admin/zones")
public class AdminAvailabilityZonesController {

    private final AvailabilityZoneService availabilityZoneService;


    public AdminAvailabilityZonesController(AvailabilityZoneService availabilityZoneService) {
        this.availabilityZoneService = availabilityZoneService;
    }

    @GetMapping("/all_zones")
    public List<AvailabilityZoneDTO> getAllAvailabilityZones() {
        return availabilityZoneService.getAllZonesDTO();
    }


}
