package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "image", schema = "cloud_v3")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "path", nullable = false, length = 45)
    private String path;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    @OneToMany(mappedBy = "image")
    private Set<VirtualMachine> virtualMachines = new LinkedHashSet<>();

    @Column(name="state", nullable = false)
    private String state;

}