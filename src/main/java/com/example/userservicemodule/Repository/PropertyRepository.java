package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.Property;
import com.example.userservicemodule.Entity.PropertyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, PropertyId> {

    Optional<Property> findById_SliceId(Integer sliceId);
}
