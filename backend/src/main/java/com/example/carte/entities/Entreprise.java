package com.example.carte.entities;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "entreprise")
public class Entreprise implements Serializable {

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
}
