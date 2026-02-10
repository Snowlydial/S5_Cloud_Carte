package com.example.carte.entities;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "configuration")
@Data
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuration")
    private Integer idConfiguration;

    @Column(name = "tentative_max")
    private Integer tentativeMax;

    @Column(name = "m2_forfaitaire")
    private Double m2Forfaitaire;

    public Configuration() {}

    public Configuration(Integer tentativeMax, Double m2Forfaitaire) {
        this.tentativeMax = tentativeMax;
        this.m2Forfaitaire = m2Forfaitaire;
    }

   
}

