package com.example.carte.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TypeSignalementDTO implements Serializable {

    private Integer idType;       // correspond à idType de l'entity
    private String nom;           // nom du type de signalement
    private String firebaseId;    // id dans Firestore
    private String idimage;
    // Optionnel : date de dernière synchronisation
    // private LocalDateTime lastSync;

}
