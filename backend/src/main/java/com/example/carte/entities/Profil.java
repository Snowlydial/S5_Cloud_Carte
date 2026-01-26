package com.example.carte.entities;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "profil")
public class Profil implements Serializable {

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
}
