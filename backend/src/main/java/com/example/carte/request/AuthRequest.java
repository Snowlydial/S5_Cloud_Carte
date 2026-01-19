package com.example.carte.request;

public class AuthRequest {

    /** Email ou username de l’utilisateur */
    private String username;

    /** Mot de passe local (offline) */
    private String password;

    /** Firebase UID (online) */
    private String firebaseUid;

    // Getters & Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}
