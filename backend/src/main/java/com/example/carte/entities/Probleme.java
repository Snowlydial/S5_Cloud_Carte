package com.example.carte.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "probleme")
public class Probleme implements Syncable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_probleme")
    private Integer idProbleme;

    @Column(name = "date_probleme", nullable = false)
    private LocalDateTime dateProbleme;

    @Column(name = "surface_m2", nullable = false)
    private Double surfaceM2;

    @Column(nullable = false)
    private Double budget;

    private Double avancement;

    @ManyToOne
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "id_compte", nullable = false)
    private User compte;

    @ManyToOne
    @JoinColumn(name = "id_signalement", nullable = false)
    private Signalement signalement;

    @OneToMany(mappedBy = "probleme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemeStatus> statusList;

    // 🔹 Attributs Firestore
    @Column(name = "firebase_id")
    private String firebaseId;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    // Constructors
    public Probleme() {
    }

    public Probleme(LocalDateTime dateProbleme, Double surfaceM2, Double budget,
                    Entreprise entreprise, User compte, Signalement signalement) {
        this.dateProbleme = dateProbleme;
        this.surfaceM2 = surfaceM2;
        this.budget = budget;
        this.entreprise = entreprise;
        this.compte = compte;
        this.signalement = signalement;
    }

    // Getters & Setters
    public Integer getIdProbleme() {
        return idProbleme;
    }

    public void setIdProbleme(Integer idProbleme) {
        this.idProbleme = idProbleme;
    }

    public LocalDateTime getDateProbleme() {
        return dateProbleme;
    }

    public void setDateProbleme(LocalDateTime dateProbleme) {
        this.dateProbleme = dateProbleme;
    }

    public Double getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(Double surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public User getCompte() {
        return compte;
    }

    public void setCompte(User compte) {
        this.compte = compte;
    }

    public Signalement getSignalement() {
        return signalement;
    }

    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
    }

    public List<ProblemeStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<ProblemeStatus> statusList) {
        this.statusList = statusList;
    }

    // 🔹 Implémentation de Syncable
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
        return "problemes"; // nom de la collection Firestore
    }
}
