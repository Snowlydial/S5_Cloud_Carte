package com.example.carte.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")  // Escaped because "user" is a reserved keyword in PostgreSQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(length = 50)
    private String role = "USER";

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public User() {}

    public User(String email, String motDePasse, String role) {
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public Boolean getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}