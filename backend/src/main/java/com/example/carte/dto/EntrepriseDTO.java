package com.example.carte.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EntrepriseDTO implements Serializable {

    private Integer idEntreprise;
    private String nom;
    private String firebaseId;
    private LocalDateTime lastSync;

}
