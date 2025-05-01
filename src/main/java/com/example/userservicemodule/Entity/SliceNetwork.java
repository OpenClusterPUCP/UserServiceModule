package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "slice_network", schema = "cloud_v3")
public class SliceNetwork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slice_id", nullable = false)
    private Slice slice;

    @NotNull
    @Column(name = "svlan_id", nullable = false)
    private Integer svlanId;

    @Size(max = 45)
    @NotNull
    @Column(name = "network", nullable = false, length = 45)
    private String network;

    @Size(max = 45)
    @NotNull
    @Column(name = "dhcp_range_start", nullable = false, length = 45)
    private String dhcpRangeStart;

    @Size(max = 45)
    @NotNull
    @Column(name = "dhcp_range_end", nullable = false, length = 45)
    private String dhcpRangeEnd;

    @Size(max = 45)
    @NotNull
    @Column(name = "slice_bridge_name", nullable = false, length = 45)
    private String sliceBridgeName;

    @Size(max = 45)
    @NotNull
    @Column(name = "patch_port_slice", nullable = false, length = 45)
    private String patchPortSlice;

    @Size(max = 45)
    @NotNull
    @Column(name = "patch_port_int", nullable = false, length = 45)
    private String patchPortInt;

    @Size(max = 45)
    @NotNull
    @Column(name = "dhcp_interface", nullable = false, length = 45)
    private String dhcpInterface;

    @Size(max = 45)
    @NotNull
    @Column(name = "gateway_interface", nullable = false, length = 45)
    private String gatewayInterface;

    @Column(name = "created_at")
    private Instant createdAt;

}