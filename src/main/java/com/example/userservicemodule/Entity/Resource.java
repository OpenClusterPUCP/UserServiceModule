package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "resource")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "cpu", nullable = false)
    private Integer cpu;

    @Column(name = "ram")
    private Integer ram;

    @Column(name = "disk")
    private Integer disk;

    @Column(name = "slices")
    private Integer slices;

    @Column(name = "used_cpu")
    private Integer usedCpu;

    @Column(name = "used_ram")
    private Integer usedRam;

    @Column(name = "used_disk")
    private Integer usedDisk;

    @Column(name = "used_slices")
    private Integer usedSlices;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;
}