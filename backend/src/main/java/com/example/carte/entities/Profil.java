package com.example.carte.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "profil")
public class Profil implements Serializable,Syncable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profil")
    private Integer idProfil;

    @Column(length = 50)
    private String nom;

    // Constructors
    public Profil() {}

    public Profil(String nom) {
        this.nom = nom;
    }

    // Getters and Setters
    public Integer getIdProfil() {
        return idProfil;
    }

    public void setIdProfil(Integer idProfil) {
        this.idProfil = idProfil;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    @Column(name = "firebase_id")
    private String firebaseId;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    // Relation inverse (optionnelle mais recommandée)
    @OneToMany(mappedBy = "typeSignalement")
    private List<Signalement> signalements;

   @Override
    public String getFirebaseId() {
        return firebaseId;
    }

    @Override
    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    @Override
    public LocalDateTime getLastSync() {
        return lastSync;
    }

    @Override
    public void setLastSync(LocalDateTime lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public String getCollectionName() {
        return "profil"; // nom de la collection Firestore
    }
}
