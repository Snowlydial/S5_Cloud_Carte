package com.example.carte.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "signalement")
public class Signalement implements Syncable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Integer idSignalement;

    @Column(name = "date_signalement", nullable = false)
    private LocalDateTime dateSignalement;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "surface_m2")
    private Double surfaceM2;

    @ManyToOne
    @JoinColumn(name = "id_compte", nullable = false)
    private User compte;
    @ManyToOne
    @JoinColumn(name = "id_type", nullable = false)
    private TypeSignalement typeSignalement;
    
    @OneToOne(mappedBy = "signalement")
    private Probleme probleme;
    @Column(name = "firebase_id")
    private String firebaseId;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    @Column(name = "description")
    private String description;

    // Constructors
    public Signalement() {
    }

    public Signalement(LocalDateTime dateSignalement, Double longitude, Double latitude, Double surfaceM2,
            User compte) {
        this.dateSignalement = dateSignalement;
        this.longitude = longitude;
        this.latitude = latitude;
        this.surfaceM2 = surfaceM2;
        this.compte = compte;
    }

    // Getters and Setters
    public Integer getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(Integer idSignalement) {
        this.idSignalement = idSignalement;
    }

    public LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(Double surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public User getCompte() {
        return compte;
    }

    public void setCompte(User compte) {
        this.compte = compte;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public LocalDateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(LocalDateTime lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public String getCollectionName() {
        return "signalements"; // Nom de la collection Firestore
    }
}
