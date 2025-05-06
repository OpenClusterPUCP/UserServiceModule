package com.example.userservicemodule.Controller;

import com.example.userservicemodule.DTO.ServerInfoDTO;
import com.example.userservicemodule.Entity.PhysicalServer;
import com.example.userservicemodule.Repository.PhysicalServerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/PhysicalServer")
public class PhysicalServerController {

    private final PhysicalServerRepository physicalServerRepository;

    public PhysicalServerController(PhysicalServerRepository physicalServerRepository) {
        this.physicalServerRepository = physicalServerRepository;
    }

    @GetMapping("/servers/by-zone/{zoneId}")
    public ResponseEntity<List<ServerInfoDTO>> getServersByZone(@PathVariable Integer zoneId) {
        List<PhysicalServer> servers = physicalServerRepository
                .findByAvailabilityZone_IdAndServerTypeIn(zoneId, List.of("headnode", "worker"));

        List<ServerInfoDTO> serverDtos = servers.stream().map(server -> new ServerInfoDTO(
                server.getIp(),
                server.getGatewayAccessIp(),
                server.getGatewayAccessPort()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(serverDtos);
    }

}
