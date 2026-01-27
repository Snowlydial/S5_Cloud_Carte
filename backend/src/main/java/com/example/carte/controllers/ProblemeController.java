package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.dto.RecapDashboardDTO;
import com.example.carte.dto.UpdateProblemeStatusRequest;
import com.example.carte.entities.Probleme;
import com.example.carte.security.SecurityUtils;
import com.example.carte.services.ProblemeService;
import com.example.carte.services.ProblemeStatusDTO;

@RestController
@RequestMapping("/api/problemes")
public class ProblemeController {

    private final ProblemeService problemeService;

    public ProblemeController(ProblemeService problemeService) {
        this.problemeService = problemeService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemeDTO>> getAllProblemesRaw() throws Exception {
        List<ProblemeDTO> liste = problemeService.getListSyncProblemes();
        return ResponseEntity.ok(liste);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Probleme> getProblemeById(@PathVariable Integer id) {
        Probleme p = problemeService.findProblemeById(id)
                .orElseThrow(() -> new RuntimeException("Problème introuvable"));
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<ProblemeDTO> createProbleme(@RequestBody ProblemeDTO dto) {

        // Récupérer l'email de l'utilisateur connecté
        String userEmail = SecurityUtils.getCurrentUserEmail();
        dto.setCompteEmail(userEmail);

        ProblemeDTO created = problemeService.createProbleme(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/sync")
    public ResponseEntity<?> syncProblemes() {
        try {
            problemeService.syncFireBaseProbleme();
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/update-status")
    public ResponseEntity<ProblemeDTO> updateProblemeStatus(
            @RequestBody UpdateProblemeStatusRequest request) {

        ProblemeDTO dto = problemeService.updateStatus(request.getIdProbleme(), request.getStatusData());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemeDTO> updateProbleme(
            @PathVariable("id") Integer id,
            @RequestBody ProblemeDTO dto) {

        ProblemeDTO updated = problemeService.updateProbleme(dto, id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/recap")
    public ResponseEntity<RecapDashboardDTO> getRecapActuel() {
        return ResponseEntity.ok(problemeService.getRecapActuel());
    }

}
