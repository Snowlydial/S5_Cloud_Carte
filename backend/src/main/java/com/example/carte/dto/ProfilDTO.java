package com.example.carte.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ProfilDTO implements Serializable {
    private Integer idProfil;
    private String nom;
    private String firebaseId;
}
