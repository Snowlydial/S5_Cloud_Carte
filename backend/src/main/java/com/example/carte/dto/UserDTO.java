package com.example.carte.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UserDTO {
    private String firebaseUid;
    private Integer idUser;
    private String email;
    private String role;
    private LocalDateTime lastSync;
    private Integer tentative;
    private boolean isBlocked;
    private String password;
    private List<String> fmcTocken;
    // Getters & Setters
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
}
