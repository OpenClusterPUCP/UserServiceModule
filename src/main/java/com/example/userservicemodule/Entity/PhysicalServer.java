package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "physical_server", schema = "cloud_v3")
public class PhysicalServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "hostname", nullable = false, length = 45)
    private String hostname;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "total_vcpu", nullable = false)
    private Integer totalVcpu;

    @Column(name = "total_disk", nullable = false)
    private Integer totalDisk;

    @Column(name = "total_ram", nullable = false)
    private Integer totalRam;

    @Column(name = "used_ram", nullable = false)
    private Integer usedRam;

    @Column(name = "used_disk", nullable = false)
    private Integer usedDisk;

    @Column(name = "used_vcpu", nullable = false)
    private Integer usedVcpu;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "infrastructure_type", nullable = false, length = 45)
    private String infrastructureType;

    @Column(name = "auth_method", nullable = false, length = 45)
    private String authMethod;

    @Column(name = "ssh_port", nullable = false, length = 45)
    private String sshPort;

    @Column(name = "ssh_username", nullable = false, length = 45)
    private String sshUsername;

    @Column(name = "ssh_password", nullable = false, length = 250)
    private String sshPassword;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_zone", nullable = false)
    private AvailabilityZone availabilityZone;

    @OneToMany(mappedBy = "physicalServer")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

}