package com.example.carte.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ProblemeStatusData {
    private String etat; // "en_cours", "termine", etc.
    private LocalDateTime dateStatus;
    private Integer idStatus;

    public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // Retourne JSON vide en cas d'erreur
        }
    }
    // getters & setters
}
