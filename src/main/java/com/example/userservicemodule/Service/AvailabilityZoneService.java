package com.example.userservicemodule.Service;

import com.example.userservicemodule.DTO.AvailabilityZoneDTO;
import com.example.userservicemodule.Entity.AvailabilityZone;
import com.example.userservicemodule.Repository.AvailabilityZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityZoneService {

    private final AvailabilityZoneRepository availabilityZoneRepository;


    public AvailabilityZoneService(AvailabilityZoneRepository availabilityZoneRepository) {
        this.availabilityZoneRepository = availabilityZoneRepository;
    }

    public List<AvailabilityZoneDTO> getAllZonesDTO() {
        return availabilityZoneRepository.findAll().stream()
                .map(zone -> new AvailabilityZoneDTO(zone.getId(), zone.getName(), zone.getDescription()))
                .toList();
    }



}
