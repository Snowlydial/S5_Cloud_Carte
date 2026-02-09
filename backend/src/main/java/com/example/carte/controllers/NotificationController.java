package com.example.carte.controllers;

import com.example.carte.services.FirebaseNotificationService;
import com.example.carte.services.UserService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final UserService firebaseUserService;
    private final FirebaseNotificationService firebaseNotificationService;

    public NotificationController(
            UserService firebaseUserService,
            FirebaseNotificationService firebaseNotificationService
    ) {
        this.firebaseUserService = firebaseUserService;
        this.firebaseNotificationService = firebaseNotificationService;
    }

    @PostMapping("/user/{uid}")
    public ResponseEntity<?> notifyUser(
            @PathVariable("uid") String firebaseUid,
            @RequestParam String title,
            @RequestParam String message
    ) {
        try {
            // 1️⃣ récupérer les tokens du user
            List<String> tokens =
                    firebaseUserService.getFcmTokensByUid(firebaseUid);

            // 2️⃣ envoyer la notification
            firebaseNotificationService.sendNotificationToTokens(
                    tokens,
                    title,
                    message
            );

            return ResponseEntity.ok("Notification envoyée avec succès 🔥");

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Erreur lors de l'envoi : " + e.getMessage());
        }
    }
}
