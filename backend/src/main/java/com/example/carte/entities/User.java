package com.example.carte.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_cloud",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_firebase_uid", columnList = "firebase_uid")
    }
)
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "firebase_uid", unique = true, length = 128)
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Mot de passe LOCAL (hashé – BCrypt)
     * Utilisé UNIQUEMENT en mode offline
     */
    @Column(name = "password_plain")
private String password;


    @Column(length = 50)
    private String role = "USER";

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;
 
    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profil", nullable = false)
    private Profil profil;


    public User() {}

    public User(String firebaseUid, String email, String password, String role) {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }


    public Integer getId() {
        return id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setIsBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public LocalDateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(LocalDateTime lastSync) {
        this.lastSync = lastSync;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }
    
    
}
