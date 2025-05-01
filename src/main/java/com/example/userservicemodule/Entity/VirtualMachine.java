package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 45)
    @NotNull
    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image", nullable = false)
    private Image image;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flavor", nullable = false)
    private Flavor flavor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slice", nullable = false)
    private Slice slice;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physical_server", nullable = false)
    private PhysicalServer physicalServer;

    @Size(max = 45)
    @NotNull
    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "vnc_port")
    private Integer vncPort;

    @Column(name = "vnc_display")
    private Integer vncDisplay;

    @Column(name = "qemu_pid")
    private Integer qemuPid;

    @OneToMany(mappedBy = "vm")
    private Set<Interface> interfaceFields = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    public Slice getSlice() {
        return slice;
    }

    public void setSlice(Slice slice) {
        this.slice = slice;
    }

    public PhysicalServer getPhysicalServer() {
        return physicalServer;
    }

    public void setPhysicalServer(PhysicalServer physicalServer) {
        this.physicalServer = physicalServer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVncPort() {
        return vncPort;
    }

    public void setVncPort(Integer vncPort) {
        this.vncPort = vncPort;
    }

    public Integer getVncDisplay() {
        return vncDisplay;
    }

    public void setVncDisplay(Integer vncDisplay) {
        this.vncDisplay = vncDisplay;
    }

    public Integer getQemuPid() {
        return qemuPid;
    }

    public void setQemuPid(Integer qemuPid) {
        this.qemuPid = qemuPid;
    }

    public Set<Interface> getInterfaceFields() {
        return interfaceFields;
    }

    public void setInterfaceFields(Set<Interface> interfaceFields) {
        this.interfaceFields = interfaceFields;
    }
}