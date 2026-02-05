package com.example.carte.dto;

import lombok.Data;

@Data
public class StatusDTO {

    private Integer idStatus;
    private String nom;
    private String firebaseId;
    public Integer getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Integer idStatus) {
        this.idStatus = idStatus;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
