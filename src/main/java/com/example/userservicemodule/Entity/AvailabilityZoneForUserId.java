package com.example.userservicemodule.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AvailabilityZoneForUserId implements Serializable {
    private static final long serialVersionUID = 7793983266805542256L;
    @NotNull
    @Column(name = "availability_zone", nullable = false)
    private Integer availabilityZone;

    @NotNull
    @Column(name = "user", nullable = false)
    private Integer user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AvailabilityZoneForUserId entity = (AvailabilityZoneForUserId) o;
        return Objects.equals(this.availabilityZone, entity.availabilityZone) &&
                Objects.equals(this.user, entity.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(availabilityZone, user);
    }

}