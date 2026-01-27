package com.example.carte.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.RecapDashboardDTO;
import com.example.carte.services.ProblemeService;
import com.example.carte.services.SignalementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Statistiques et récapitulations globales")
public class DashboardController {

    private final SignalementService signalementService;
    
    @Autowired
    private ProblemeService problemeService;

    public DashboardController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }

    @Operation(
        summary = "Voir le tableau de récapitulation actuel",
        description = "Retourne le nombre de points, surface totale, avancement (%) et budget total"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Récapitulatif récupéré avec succès",
        content = @Content(schema = @Schema(implementation = RecapDashboardDTO.class))
    )
    @GetMapping("/recap")
    public ResponseEntity<RecapDashboardDTO> getRecapActuel() {
        return ResponseEntity.ok(problemeService.getRecapActuel());
    }
}
