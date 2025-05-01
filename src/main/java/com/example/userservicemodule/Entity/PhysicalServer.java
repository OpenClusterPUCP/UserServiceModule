package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 45)
    @NotNull
    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Size(max = 45)
    @NotNull
    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Size(max = 45)
    @NotNull
    @Column(name = "data_ip", nullable = false, length = 45)
    private String dataIp;

    @NotNull
    @Column(name = "total_vcpu", nullable = false)
    private Integer totalVcpu;

    @NotNull
    @Column(name = "total_disk", nullable = false)
    private Integer totalDisk;

    @NotNull
    @Column(name = "total_ram", nullable = false)
    private Integer totalRam;

    @NotNull
    @Column(name = "used_ram", nullable = false)
    private Integer usedRam;

    @NotNull
    @Column(name = "used_disk", nullable = false)
    private Integer usedDisk;

    @NotNull
    @Column(name = "used_vcpu", nullable = false)
    private Integer usedVcpu;

    @Size(max = 45)
    @NotNull
    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Size(max = 45)
    @NotNull
    @Column(name = "infrastructure_type", nullable = false, length = 45)
    private String infrastructureType;

    @Size(max = 45)
    @NotNull
    @Column(name = "auth_method", nullable = false, length = 45)
    private String authMethod;

    @NotNull
    @Column(name = "ssh_port", nullable = false)
    private Integer sshPort;

    @Size(max = 45)
    @NotNull
    @Column(name = "ssh_username", nullable = false, length = 45)
    private String sshUsername;

    @Size(max = 250)
    @NotNull
    @Column(name = "ssh_password", nullable = false, length = 250)
    private String sshPassword;

    @Size(max = 255)
    @Column(name = "ssh_key_path")
    private String sshKeyPath;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_zone", nullable = false)
    private AvailabilityZone availabilityZone;

    @Size(max = 45)
    @Column(name = "gateway_access_ip", length = 45)
    private String gatewayAccessIp;

    @Column(name = "gateway_access_port")
    private Integer gatewayAccessPort;

    @Size(max = 45)
    @NotNull
    @Column(name = "switch_name", nullable = false, length = 45)
    private String switchName;

    @NotNull
    @Lob
    @Column(name = "server_type", nullable = false)
    private String serverType;

    @OneToMany(mappedBy = "physicalServer")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

}