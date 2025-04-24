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
public class PropertyId implements Serializable {
    private static final long serialVersionUID = 301102125310549064L;
    @Column(name = "user", nullable = false)
    private Integer user;

    @Column(name = "slice", nullable = false)
    private Integer slice;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PropertyId entity = (PropertyId) o;
        return Objects.equals(this.slice, entity.slice) &&
                Objects.equals(this.user, entity.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slice, user);
    }

}