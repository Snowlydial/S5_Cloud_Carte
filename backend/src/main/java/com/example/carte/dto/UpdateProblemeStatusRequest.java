package com.example.carte.dto;

import lombok.Data;

@Data
public class UpdateProblemeStatusRequest {
    private Integer idProbleme;          // ID du problème à mettre à jour
    private ProblemeStatusData statusData; // données du nouveau statut
}
