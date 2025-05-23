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
public class PropertyId implements Serializable {
    @NotNull
    @Column(name = "slice", nullable = false)
    private Integer sliceId;

    @NotNull
    @Column(name = "user", nullable = false)
    private Integer userId;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PropertyId entity = (PropertyId) o;
        return Objects.equals(this.sliceId, entity.sliceId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sliceId, userId);
    }
}
