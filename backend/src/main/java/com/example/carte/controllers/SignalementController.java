package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.SignalementDTO;
import com.example.carte.services.SignalementService;

@RestController
@RequestMapping("/api/signalements")
@CrossOrigin(origins = "*")
public class SignalementController {

    private final SignalementService signalementService;

    public SignalementController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }

    @GetMapping
    public ResponseEntity<List<SignalementDTO>> getAllSignalements() {

        List<SignalementDTO> signalements = signalementService.getAllSignalements();
        return ResponseEntity.ok(signalements);
    }

    // /** GET : Signalement par ID */
    // @GetMapping("/{id}")
    // public ResponseEntity<SignalementDTO> getSignalementById(@PathVariable Integer id,
    //         @RequestParam(required = false) String firebaseUid,
    //         @RequestParam(required = false) String email) {

    //     SignalementDTO signalement = signalementService.findSignalementByIdHybrid(id, firebaseUid, email)
    //             .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
    //     return ResponseEntity.ok(signalement);
    // }

    // /** POST : Créer un nouveau signalement */
    // @PostMapping
    // public ResponseEntity<SignalementDTO> createSignalement(@RequestBody SignalementDTO signalementDto) {
    //     SignalementDTO saved = signalementService.createSignalementHybrid(signalementDto);
    //     return ResponseEntity.ok(saved);
    // }
}
