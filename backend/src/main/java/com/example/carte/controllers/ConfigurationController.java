package com.example.carte.controllers;

import org.springframework.web.bind.annotation.*;

import com.example.carte.entities.Configuration;
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
            @RequestParam("surface") double surfaceM2,
            @RequestParam("niveau") int niveau) {

        double budget = configurationService.calculerBudget(surfaceM2, niveau);
        return ResponseEntity.ok(budget);
    }

    @GetMapping
    public ResponseEntity<Configuration> getConfiguration() {
        Configuration config = configurationService.getConfiguration();
        return ResponseEntity.ok(config);
    }

    // Optionnel : mettre à jour la configuration (PUT)
    // @PutMapping
    // public ResponseEntity<Configuration> updateConfiguration(@RequestBody
    // Configuration config) {
    // Configuration updated = configurationService.updateConfiguration(config);
    // return ResponseEntity.ok(updated);
    // }
    @PutMapping
    public ResponseEntity<Configuration> updateConfiguration(
            @RequestParam(required = false) Integer tentativeMax,
            @RequestParam(required = false) Double m2Forfaitaire) {

        Configuration updatedConfig = configurationService.updateConfiguration(tentativeMax, m2Forfaitaire);
        return ResponseEntity.ok(updatedConfig);
    }
}
