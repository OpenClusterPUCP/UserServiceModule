package com.example.userservicemodule.Service;

import com.example.userservicemodule.DTO.AvailabilityZoneDTO;
import com.example.userservicemodule.Entity.AvailabilityZone;
import com.example.userservicemodule.Entity.Flavor;
import com.example.userservicemodule.Entity.VirtualMachine;
import com.example.userservicemodule.Repository.AvailabilityZoneRepository;
import com.example.userservicemodule.Repository.VirtualMachineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityZoneService {

    private final AvailabilityZoneRepository availabilityZoneRepository;


    public AvailabilityZoneService(AvailabilityZoneRepository availabilityZoneRepository, VirtualMachineRepository virtualMachineRepository) {
        this.availabilityZoneRepository = availabilityZoneRepository;
    }

    /*public List<AvailabilityZoneDTO> getAllZonesDTO() {
        return availabilityZoneRepository.findAll().stream().map(zone -> {
            int totalVcpu = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalVcpu() != null ? s.getTotalVcpu() : 0).sum();
            int usedVcpu = zone.getPhysicalServers().stream().mapToInt(s -> s.getUsedVcpu() != null ? s.getUsedVcpu() : 0).sum();
            int totalRam = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalRam() != null ? s.getTotalRam() : 0).sum();
            int usedRam = zone.getPhysicalServers().stream().mapToInt(s -> s.getUsedRam() != null ? s.getUsedRam() : 0).sum();
            int totalDisk = zone.getPhysicalServers().stream().mapToInt(s -> s.getTotalDisk() != null ? s.getTotalDisk() : 0).sum();
            int usedDisk = zone.getPhysicalServers().stream().mapToInt(s -> s.getUsedDisk() != null ? s.getUsedDisk() : 0).sum();
            int serverCount = zone.getPhysicalServers().size();
            int sliceCount = zone.getSlices() != null ? zone.getSlices().size() : 0;
            int totalVMs = zone.getSlices() != null
                    ? zone.getSlices().stream()
                    .mapToInt(slice -> slice.getVirtualMachines() != null ? slice.getVirtualMachines().size() : 0)
                    .sum()
                    : 0;


            return new AvailabilityZoneDTO(
                    zone.getId(), zone.getName(), zone.getDescription(),
                    totalVcpu, usedVcpu, totalRam, usedRam, totalDisk, usedDisk,
                    serverCount, sliceCount, totalVMs
            );
        }).toList();
    }*/

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







}
