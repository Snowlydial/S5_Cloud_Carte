package com.example.carte.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "probleme_status")
public class ProblemeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_probleme_status")
    private Integer idProblemeStatus;

    @Column(length = 50)
    private String etat;

    @Column(name = "date_status")
    private LocalDateTime dateStatus;

    @ManyToOne
    @JoinColumn(name = "id_probleme", nullable = false)
    private Probleme probleme;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    // Constructors
    public ProblemeStatus() {}

    public ProblemeStatus(String etat, LocalDateTime dateStatus, Probleme probleme, Status status) {
        this.etat = etat;
        this.dateStatus = dateStatus;
        this.probleme = probleme;
        this.status = status;
    }

    // Getters and Setters
    public Integer getIdProblemeStatus() {
        return idProblemeStatus;
    }

    public void setIdProblemeStatus(Integer idProblemeStatus) {
        this.idProblemeStatus = idProblemeStatus;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDateTime getDateStatus() {
        return dateStatus;
    }

    public void setDateStatus(LocalDateTime dateStatus) {
        this.dateStatus = dateStatus;
    }

    public Probleme getProbleme() {
        return probleme;
    }

    public void setProbleme(Probleme probleme) {
        this.probleme = probleme;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
