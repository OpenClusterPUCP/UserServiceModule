package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "property", schema = "cloud_v3")
public class Property {
    @EmbeddedId
    private PropertyId id;

    @MapsId("user")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @MapsId("slice")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slice", nullable = false)
    private Slice slice;

}