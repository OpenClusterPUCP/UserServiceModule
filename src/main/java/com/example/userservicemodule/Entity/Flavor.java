package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "flavor", schema = "cloud_v3")
public class Flavor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "ram", nullable = false)
    private Integer ram;

    @Column(name = "vcpu", nullable = false)
    private Integer vcpu;

    @Column(name = "disk", nullable = false)
    private Integer disk;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    @OneToMany(mappedBy = "flavor")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

    @Column(name="state" , nullable = false)
    private String state;

}