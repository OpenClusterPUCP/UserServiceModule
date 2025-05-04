package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "slice", schema = "cloud_v3")
public class Slice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    //@Column(name = "type", nullable = false, length = 45)
    //private String type; No existe "type" en Slice

    @Column(name = "description", nullable = false, length = 250)
    private String description;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }



    @ManyToMany(mappedBy = "slices")
    private Set<AvailabilityZone> availabilityZones = new LinkedHashSet<>();

    @OneToMany(mappedBy = "slice")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

}