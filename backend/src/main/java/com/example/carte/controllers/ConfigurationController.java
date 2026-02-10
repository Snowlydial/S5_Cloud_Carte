package com.example.carte.controllers;


import org.springframework.web.bind.annotation.*;

import com.example.carte.services.ConfigurationService;

import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/calcul-budget")
    public ResponseEntity<Double> calculerBudget(
            @RequestParam("niveau") int niveau) {

        double budget = configurationService.calculerBudget( niveau);
        return ResponseEntity.ok(budget);
    }
}

