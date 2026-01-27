package com.example.carte.request;

import lombok.Data;

@Data
public class AuthRegisterRequest {

    private String email;
    private String password;      // utilisé offline
    private String firebaseUid;   // fourni si online
    private String role;
    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
}
