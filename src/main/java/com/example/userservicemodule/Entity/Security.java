package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "security", schema = "cloud_v3")
public class Security {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "rule", nullable = false, length = 45)
    private String rule;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @Column(name = "port_range_min", nullable = false, length = 45)
    private String portRangeMin;

    @Column(name = "port_range_max", nullable = false, length = 45)
    private String portRangeMax;

    @Column(name = "remote_ip_prefix", nullable = false, length = 45)
    private String remoteIpPrefix;

    @Column(name = "direction", nullable = false, length = 45)
    private String direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "securoty")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

}