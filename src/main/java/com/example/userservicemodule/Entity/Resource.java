package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

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

    @Column(name = "disk", precision = 10, scale = 2)
    private BigDecimal disk;

    @Column(name = "slices")
    private Integer slices;

    @Column(name = "used_cpu")
    private Integer usedCpu;

    @Column(name = "used_ram")
    private Integer usedRam;

    @Column(name = "used_disk", precision = 10, scale = 2)
    private BigDecimal usedDisk;

    @Column(name = "used_slices")
    private Integer usedSlices;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;
}