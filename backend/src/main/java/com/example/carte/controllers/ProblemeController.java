package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.entities.Probleme;
import com.example.carte.security.SecurityUtils;
import com.example.carte.services.ProblemeService;

@RestController
@RequestMapping("/api/problemes")
public class ProblemeController {

    private final ProblemeService problemeService;

    public ProblemeController(ProblemeService problemeService) {
        this.problemeService = problemeService;
    }


    @GetMapping
    public ResponseEntity<List<Probleme>> getAllProblemesRaw() {
        List<Probleme> liste = problemeService.getAllProblemesRaw();
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

}
