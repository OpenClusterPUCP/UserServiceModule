package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 45)
    @NotNull
    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Size(max = 45)
    @Column(name = "mac", length = 45)
    private String mac;

    @Size(max = 45)
    @Column(name = "ip", length = 45)
    private String ip;

    @NotNull
    @Column(name = "external_access", nullable = false)
    private Boolean externalAccess = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vm", nullable = false)
    private VirtualMachine vm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link")
    private Link link;

    @Size(max = 45)
    @Column(name = "tap_name", length = 45)
    private String tapName;

}