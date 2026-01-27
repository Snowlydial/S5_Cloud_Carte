package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.StatusDTO;
import com.example.carte.services.StatusService;

@RestController
@RequestMapping("/api/status")
@CrossOrigin(origins = "*")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    // ===============================
    // GET : liste des status
    // ===============================
    @GetMapping
    public ResponseEntity<List<StatusDTO>> getAllStatus() {
        List<StatusDTO> statusList = statusService.getAllStatus();
        return ResponseEntity.ok(statusList);
    }
}
