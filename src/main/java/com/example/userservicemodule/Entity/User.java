package com.example.userservicemodule.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user", schema = "cloud_v3")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, length = 45)
    private String username;

    @Column(name = "password", nullable = false, length = 260)
    private String password;

    @Column(name = "code", nullable = false, length = 45)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    @Column(name = "state", nullable = false, length = 45)
    private String state;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "lastname", nullable = false, length = 45)
    private String lastname;

    @Column(name = "created_At")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

}