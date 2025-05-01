package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "availability_zone_for_user", schema = "cloud_v3")
public class AvailabilityZoneForUser {
    @EmbeddedId
    private AvailabilityZoneForUserId id;

    @MapsId("availabilityZone")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_zone", nullable = false)
    private AvailabilityZone availabilityZone;

    @MapsId("user")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;

}