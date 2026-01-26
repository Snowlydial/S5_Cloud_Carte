package com.example.carte.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "type_signalement")
public class TypeSignalement implements Serializable,Syncable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type")
    private Integer idType;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;
    
    // Relation inverse (optionnelle mais recommandée)
    @OneToMany(mappedBy = "typeSignalement")
    private List<Signalement> signalements;

    @Column(name = "firebase_id")
    private String firebaseId;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

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
        return "type_signalement"; // nom de la collection Firestore
    }
    public void setIdType(Integer idType) {
        this.idType = idType;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public void setSignalements(List<Signalement> signalements) {
        this.signalements = signalements;
    }
    public Integer getIdType() {
        return idType;
    }
    public String getNom() {
        return nom;
    }
    public List<Signalement> getSignalements() {
        return signalements;
    }
}
