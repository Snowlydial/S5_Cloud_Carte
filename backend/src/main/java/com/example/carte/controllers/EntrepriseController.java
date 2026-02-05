package com.example.carte.controllers;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.EntrepriseDTO;
import com.example.carte.services.EntrepriseService;

@RestController
@RequestMapping("/api/entreprises")
@CrossOrigin(origins = "*")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    // ===============================
    // GET : liste des entreprises (sync Firebase si online)
    // ===============================
    @GetMapping
    public ResponseEntity<List<EntrepriseDTO>> getAllEntreprises() throws InterruptedException, ExecutionException {
        List<EntrepriseDTO> entreprises = entrepriseService.getListSyncEntreprises();
        return ResponseEntity.ok(entreprises);
    }

    // ===============================
    // GET : entreprise par ID
    // ===============================
    // @GetMapping("/{id}")
    // public ResponseEntity<EntrepriseDTO> getEntrepriseById(@PathVariable Integer id) {
    //     EntrepriseDTO dto = entrepriseService.getEntrepriseById(id);
    //     return ResponseEntity.ok(dto);
    // }

    // ===============================
    // POST : créer une entreprise
    // ===============================
    // @PostMapping
    // public ResponseEntity<EntrepriseDTO> createEntreprise(@RequestBody EntrepriseDTO dto) {
    //     EntrepriseDTO created = entrepriseService.createEntreprise(dto);
    //     return new ResponseEntity<>(created, HttpStatus.CREATED);
    // }

    // ===============================
    // PUT : mise à jour
    // ===============================
    // @PutMapping("/{id}")
    // public ResponseEntity<EntrepriseDTO> updateEntreprise(
    //         @PathVariable Integer id,
    //         @RequestBody EntrepriseDTO dto) {

    //     EntrepriseDTO updated = entrepriseService.updateEntreprise(id, dto);
    //     return ResponseEntity.ok(updated);
    // }

    // // ===============================
    // // DELETE : suppression
    // // ===============================
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteEntreprise(@PathVariable Integer id) {
    //     entrepriseService.deleteEntreprise(id);
    //     return ResponseEntity.noContent().build();
    // }

    // // ===============================
    // // POST : forcer la synchronisation Firebase
    // // ===============================
    // @PostMapping("/sync")
    // public ResponseEntity<List<EntrepriseDTO>> syncEntreprises() {
    //     List<EntrepriseDTO> synced = entrepriseService.getListSyncEntreprises();
    //     return ResponseEntity.ok(synced);
    // }
}
