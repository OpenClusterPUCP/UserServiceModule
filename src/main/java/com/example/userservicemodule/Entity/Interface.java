package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "interface", schema = "cloud_v3")
public class Interface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "mac", nullable = false, length = 45)
    private String mac;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "external_access", nullable = false, length = 45)
    private String externalAccess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vm", nullable = false)
    private VirtualMachine vm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link", nullable = false)
    private Link link;

}