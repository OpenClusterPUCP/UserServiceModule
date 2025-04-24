package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "log", schema = "cloud_v3")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @Column(name = "description", nullable = false, length = 45)
    private String description;

    @Column(name = "time", nullable = false)
    private Instant time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user", nullable = false)
    private User user;

}