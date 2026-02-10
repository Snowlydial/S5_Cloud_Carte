package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.RecapDashboardDTO;
import com.example.carte.dto.SignalementDTO;
import com.example.carte.dto.StatusDTO;
import com.example.carte.services.ProblemeService;
import com.example.carte.services.SignalementService;
import com.example.carte.services.StatusService;

/**
 * Public API endpoints for guest/visitor access (no authentication required)
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private final SignalementService signalementService;
    private final ProblemeService problemeService;
    private final StatusService statusService;

    public PublicController(
            SignalementService signalementService,
            ProblemeService problemeService,
            StatusService statusService) {
        this.signalementService = signalementService;
        this.problemeService = problemeService;
        this.statusService = statusService;
    }

    /**
     * GET: List all signalements with their problems (public access)
     */
    @GetMapping("/signalements")
    public ResponseEntity<List<SignalementDTO>> getPublicSignalements() {
        try {
            List<SignalementDTO> signalements = signalementService.getAllSignalements();
            return ResponseEntity.ok(signalements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET: Recap dashboard stats (public access)
     */
    @GetMapping("/recap")
    public ResponseEntity<RecapDashboardDTO> getPublicRecap() {
        try {
            RecapDashboardDTO recap = problemeService.getRecapActuel();
            return ResponseEntity.ok(recap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET: Status list (public access)
     */
    @GetMapping("/status")
    public ResponseEntity<List<StatusDTO>> getPublicStatusList() {
        try {
            List<StatusDTO> statusList = statusService.getAllStatus();
            return ResponseEntity.ok(statusList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
