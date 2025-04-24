package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "availability_zone", schema = "cloud_v3")
public class AvailabilityZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "description", nullable = false, length = 45)
    private String description;

    @OneToMany(mappedBy = "availabilityZone")
    private Set<PhysicalServer> physicalServers = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "slice_availability_zone",
            joinColumns = @JoinColumn(name = "availability_zone"),
            inverseJoinColumns = @JoinColumn(name = "slice"))
    private Set<Slice> slices = new LinkedHashSet<>();

}