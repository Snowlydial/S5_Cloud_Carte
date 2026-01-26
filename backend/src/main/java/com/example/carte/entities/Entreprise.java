package com.example.carte.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "entreprise")
public class Entreprise implements Serializable,Syncable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entreprise")
    private Integer idEntreprise;

    @Column(length = 50)
    private String nom;

    // Constructors
    public Entreprise() {}

    public Entreprise(String nom) {
        this.nom = nom;
    }

    // Getters and Setters
    public Integer getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(Integer idEntreprise) {
        this.idEntreprise = idEntreprise;
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
        return "entreprise"; // nom de la collection Firestore
    }
}
