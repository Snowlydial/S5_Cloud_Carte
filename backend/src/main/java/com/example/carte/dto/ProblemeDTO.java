package com.example.carte.dto;

import java.time.LocalDateTime;

public class ProblemeDTO {

    private Integer idProbleme;
    private LocalDateTime dateProbleme;
    private Double surfaceM2;
    private Double budget;
    private String entrepriseNom;
    private String compteEmail;
    private Integer signalementId;
    private String statut; // un seul statut

    // 🔹 Getters & Setters
    public Integer getIdProbleme() { return idProbleme; }
    public void setIdProbleme(Integer idProbleme) { this.idProbleme = idProbleme; }

    public LocalDateTime getDateProbleme() { return dateProbleme; }
    public void setDateProbleme(LocalDateTime dateProbleme) { this.dateProbleme = dateProbleme; }

    public Double getSurfaceM2() { return surfaceM2; }
    public void setSurfaceM2(Double surfaceM2) { this.surfaceM2 = surfaceM2; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getEntrepriseNom() { return entrepriseNom; }
    public void setEntrepriseNom(String entrepriseNom) { this.entrepriseNom = entrepriseNom; }

    public String getCompteEmail() { return compteEmail; }
    public void setCompteEmail(String compteEmail) { this.compteEmail = compteEmail; }

    public Integer getSignalementId() { return signalementId; }
    public void setSignalementId(Integer signalementId) { this.signalementId = signalementId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
