package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "slice_availability_zone", schema = "cloud_v3")
public class SliceAvailabilityZone {
    @EmbeddedId
    private SliceAvailabilityZoneId id;

    @MapsId("slice")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slice", nullable = false)
    private Slice slice;

    @MapsId("availabilityZone")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_zone", nullable = false)
    private AvailabilityZone availabilityZone;

}