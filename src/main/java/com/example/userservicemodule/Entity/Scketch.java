package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "scketch", schema = "cloud_v3")
public class Scketch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "structure", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> structure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;

}