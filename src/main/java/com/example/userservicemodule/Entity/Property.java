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


}