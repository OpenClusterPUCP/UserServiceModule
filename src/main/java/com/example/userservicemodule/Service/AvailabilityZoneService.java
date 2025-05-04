package com.example.userservicemodule.Service;

import com.example.userservicemodule.DTO.AvailabilityZoneDTO;
import com.example.userservicemodule.DTO.ZoneDetails.PhysicalServerDTO;
import com.example.userservicemodule.DTO.ZoneDetails.SliceDTO;
import com.example.userservicemodule.DTO.ZoneDetails.ZoneDetailDTO;
import com.example.userservicemodule.Entity.AvailabilityZone;
import com.example.userservicemodule.Entity.Flavor;
import com.example.userservicemodule.Entity.Property;
import com.example.userservicemodule.Entity.VirtualMachine;
import com.example.userservicemodule.Repository.AvailabilityZoneRepository;
import com.example.userservicemodule.Repository.PropertyRepository;
import com.example.userservicemodule.Repository.UserRepository;
import com.example.userservicemodule.Repository.VirtualMachineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AvailabilityZoneService {

    private final AvailabilityZoneRepository availabilityZoneRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;


    public AvailabilityZoneService(AvailabilityZoneRepository availabilityZoneRepository, VirtualMachineRepository virtualMachineRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.availabilityZoneRepository = availabilityZoneRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public List<AvailabilityZoneDTO> getAllZonesDTO() {
        return availabilityZoneRepository.findAll().stream().map(zone -> {
            int totalVcpu = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalVcpu() != null ? s.getTotalVcpu() : 0).sum();
            int totalRam = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalRam() != null ? s.getTotalRam() : 0).sum();
            int totalDisk = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalDisk() != null ? s.getTotalDisk() : 0).sum();
            int serverCount = zone.getPhysicalServers().size();
            int sliceCount = zone.getSlices() != null ? zone.getSlices().size() : 0;

            List<VirtualMachine> vmsActivas = zone.getPhysicalServers().stream()
                    .flatMap(server -> server.getVirtualMachines().stream())
                    //.filter(vm -> "running".equalsIgnoreCase(vm.getStatus())) Se consideran todas las VMs instanciadas
                    .toList();

            int usedVcpu = 0;
            int usedRam = 0;
            BigDecimal usedDisk = BigDecimal.ZERO;

            for (VirtualMachine vm : vmsActivas) {
                Flavor flavor = vm.getFlavor();
                if (flavor != null) {
                    usedVcpu += flavor.getVcpu();
                    usedRam += flavor.getRam();
                    usedDisk = usedDisk.add(flavor.getDisk());
                }
            }

            int totalVMs = vmsActivas.size();

            return new AvailabilityZoneDTO(
                    zone.getId(), zone.getName(), zone.getDescription(),
                    totalVcpu, usedVcpu, totalRam, usedRam,
                    totalDisk, usedDisk.intValue(),
                    serverCount, sliceCount, totalVMs
            );
        }).toList();
    }

    public ZoneDetailDTO getZoneDetailById(Integer id) {
        AvailabilityZone zone = availabilityZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        AtomicInteger totalVMs = new AtomicInteger(0);
        AtomicInteger usedVcpu = new AtomicInteger(0);
        AtomicInteger usedRam = new AtomicInteger(0);
        AtomicInteger usedDisk = new AtomicInteger(0);

        // Servidores físicos
        List<PhysicalServerDTO> serverDTOs = zone.getPhysicalServers().stream().map(server -> {
            PhysicalServerDTO dto = new PhysicalServerDTO();
            dto.setId(server.getId());
            dto.setHostname(server.getName());
            dto.setIp(server.getIp());
            dto.setStatus(server.getStatus());
            dto.setTotalVcpu(server.getTotalVcpu());
            dto.setUsedVcpu(server.getUsedVcpu());
            dto.setTotalRam(server.getTotalRam());
            dto.setUsedRam(server.getUsedRam());
            dto.setTotalDisk(server.getTotalDisk());
            dto.setUsedDisk(server.getUsedDisk());

            int vmCount = server.getVirtualMachines() != null ? server.getVirtualMachines().size() : 0;
            dto.setVmCount(vmCount);
            totalVMs.addAndGet(vmCount);

            if (server.getVirtualMachines() != null) {
                for (VirtualMachine vm : server.getVirtualMachines()) {
                    if (vm.getFlavor() != null) {
                        usedVcpu.addAndGet(vm.getFlavor().getVcpu());
                        usedRam.addAndGet(vm.getFlavor().getRam());
                        usedDisk.addAndGet(vm.getFlavor().getDisk().intValue());
                    }
                }
            }

            return dto;
        }).toList();

        // Slices
        List<SliceDTO> sliceDTOs = zone.getSlices().stream().map(slice -> {
            SliceDTO dto = new SliceDTO();
            dto.setId(slice.getId());
            dto.setName(slice.getName());
            dto.setStatus(slice.getStatus());

            if (slice.getCreatedAt() != null) {
                dto.setCreatedDate(
                        slice.getCreatedAt()
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                );
            }

            int vmCount = slice.getVirtualMachines() != null ? slice.getVirtualMachines().size() : 0;
            dto.setVmCount(vmCount);

            int sliceVcpu = 0, sliceRam = 0, sliceDisk = 0;
            if (slice.getVirtualMachines() != null) {
                for (VirtualMachine vm : slice.getVirtualMachines()) {
                    if (vm.getFlavor() != null) {
                        sliceVcpu += vm.getFlavor().getVcpu();
                        sliceRam += vm.getFlavor().getRam();
                        sliceDisk += vm.getFlavor().getDisk().intValue();
                    }
                }
            }

            dto.setAssignedVcpu(sliceVcpu);
            dto.setAssignedRam(sliceRam);
            dto.setAssignedDisk(sliceDisk);

            // Buscar propietario desde tabla property
            propertyRepository.findById_SliceId(slice.getId()).ifPresent(property -> {
                Integer userId = property.getId().getUserId();
                userRepository.findById(userId).ifPresent(user -> {
                    dto.setOwner(user.getName()); // o getUsername() si prefieres
                });
            });

            return dto;
        }).toList();

        // DTO principal
        ZoneDetailDTO detailDTO = new ZoneDetailDTO();
        detailDTO.setId(zone.getId());
        detailDTO.setName(zone.getName());
        detailDTO.setDescription(zone.getDescription());
        detailDTO.setServers(serverDTOs);
        detailDTO.setSlices(sliceDTOs);

        detailDTO.setServerCount(serverDTOs.size());
        detailDTO.setTotalSlices(sliceDTOs.size());
        detailDTO.setTotalVMs(totalVMs.get());
        detailDTO.setUsedVcpu(usedVcpu.get());
        detailDTO.setUsedRam(usedRam.get());
        detailDTO.setUsedDisk(usedDisk.get());

        // También puedes cargar totales físicos desde los objetos físicos
        detailDTO.setTotalVcpu(zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalVcpu() != null ? s.getTotalVcpu() : 0).sum());
        detailDTO.setTotalRam(zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalRam() != null ? s.getTotalRam() : 0).sum());
        detailDTO.setTotalDisk(zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalDisk() != null ? s.getTotalDisk() : 0).sum());

        return detailDTO;
    }
















}
