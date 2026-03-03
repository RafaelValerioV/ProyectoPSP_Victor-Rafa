package com.drivepsp.drivepsp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa a un usuario registrado en DrivePSP.
 * Cada usuario tiene su propio espacio de almacenamiento con un limite
 * configurable (por defecto 100 MB).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(name = "storage_used", nullable = false)
    private long storageUsed = 0;

    @Column(name = "storage_limit", nullable = false)
    private long storageLimit = 104857600; // 100 MB

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStorageUsed() {
        return storageUsed;
    }

    public void setStorageUsed(long storageUsed) {
        this.storageUsed = storageUsed;
    }

    public long getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(long storageLimit) {
        this.storageLimit = storageLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
