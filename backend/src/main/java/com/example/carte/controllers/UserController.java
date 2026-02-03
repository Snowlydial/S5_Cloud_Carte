package com.example.carte.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.BlockedUserDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.services.UserService;
import com.google.firebase.auth.FirebaseAuthException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Lister tous les utilisateurs")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(userService.getListSyncComptes());
    }

    @Operation(summary = "Lister les utilisateurs bloqués")
    @GetMapping("/blocked")
    public ResponseEntity<List<BlockedUserDTO>> getBlockedUsers() {
        return ResponseEntity.ok(userService.getBlockedUsers());
    }
    @PostMapping("/unlock/{email}")
    public ResponseEntity<Void> unlockUser(
            @PathVariable @Parameter(description = "Email de l'utilisateur à débloquer", example = "user@mail.com") String email) {

        userService.resetLoginAttempts(email);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/sync")
    public ResponseEntity<Void> sync() throws InterruptedException, ExecutionException{
        List<UserDTO> users = userService.getListSyncComptes();
        return null;
        
    }

    @PutMapping("/{userId}/email")
    public ResponseEntity<?> updateUserEmail(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> body) {

        try {
            String newEmail = body.get("newEmail");
            if (newEmail == null || newEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Le champ 'newEmail' est obligatoire");
            }

            UserDTO updatedUser = userService.updateUserEmail(userId, newEmail);
            return ResponseEntity.ok(updatedUser);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body("Erreur Firebase: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }
}
