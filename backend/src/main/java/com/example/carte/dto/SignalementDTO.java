package com.example.carte.dto;

import java.time.LocalDateTime;

import com.example.carte.entities.Probleme;

import lombok.Data;

@Data
public class SignalementDTO {

    private Integer idSignalement;
    private LocalDateTime dateSignalement;
    private Double longitude;
    private Double latitude;
    private Double surfaceM2;
    private String firebaseId;
    private String compteEmail; // pour identifier le user associé
    private String description;
    private String idTypeSignalement;
    private ProblemeDTO problemeDTO;
    private String idCompte;
    // Getters et Setters
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

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getCompteEmail() {
        return compteEmail;
    }

    public void setCompteEmail(String compteEmail) {
        this.compteEmail = compteEmail;
    }
}
