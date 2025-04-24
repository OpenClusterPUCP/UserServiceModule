package com.example.userservicemodule.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SliceAvailabilityZoneId implements Serializable {
    private static final long serialVersionUID = -4791174289935726723L;
    @Column(name = "slice", nullable = false)
    private Integer slice;

    @Column(name = "availability_zone", nullable = false)
    private Integer availabilityZone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SliceAvailabilityZoneId entity = (SliceAvailabilityZoneId) o;
        return Objects.equals(this.slice, entity.slice) &&
                Objects.equals(this.availabilityZone, entity.availabilityZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slice, availabilityZone);
    }

}