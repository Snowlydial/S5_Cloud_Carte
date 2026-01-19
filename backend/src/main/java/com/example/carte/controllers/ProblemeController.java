package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.entities.Probleme;
import com.example.carte.services.ProblemeService;

@RestController
@RequestMapping("/api/problemes")
public class ProblemeController {

    private final ProblemeService problemeService;

    public ProblemeController(ProblemeService problemeService) {
        this.problemeService = problemeService;
    }

    /** GET : Récupère tous les problèmes pour un utilisateur (hybride Firebase/local) */
    // @GetMapping
    // public ResponseEntity<List<ProblemeDTO>> getAllProblemes(
    //         @RequestParam(required = false) String firebaseUid,
    //         @RequestParam(required = false) String email) {

    //     List<ProblemeDTO> liste = problemeService.getProblemesHybrid(firebaseUid, email);
    //     return ResponseEntity.ok(liste);
    // }

    @GetMapping
    public ResponseEntity<List<Probleme>> getAllProblemesRaw() {
        List<Probleme> liste = problemeService.getAllProblemesRaw();
        return ResponseEntity.ok(liste);
    }
    /** GET : Problème par ID (local) */
    @GetMapping("/{id}")
    public ResponseEntity<Probleme> getProblemeById(@PathVariable Integer id) {
        Probleme p = problemeService.findProblemeById(id)
        .orElseThrow(() -> new RuntimeException("Problème introuvable"));
        return ResponseEntity.ok(p);
    }

    /** POST : Créer un problème */
    @PostMapping
    public ResponseEntity<Probleme> createProbleme(@RequestBody Probleme probleme) {
        Probleme saved = problemeService.createProbleme(probleme);
        return ResponseEntity.ok(saved);
    }
}
