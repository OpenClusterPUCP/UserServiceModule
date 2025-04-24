package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "virtual_machine", schema = "cloud_v3")
public class VirtualMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image", nullable = false)
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flavor", nullable = false)
    private Flavor flavor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slice", nullable = false)
    private Slice slice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "securoty", nullable = false)
    private Security securoty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physical_server", nullable = false)
    private PhysicalServer physicalServer;

    @OneToMany(mappedBy = "vm")
    private Set<Interface> interfaceFields = new LinkedHashSet<>();

}