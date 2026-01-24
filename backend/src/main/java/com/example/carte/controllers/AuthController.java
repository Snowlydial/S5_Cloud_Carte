package com.example.carte.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.JwtResponse;
import com.example.carte.dto.LoginDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.User;
import com.example.carte.request.AuthRegisterRequest;
import com.example.carte.request.AuthRequest;
import com.example.carte.security.JwtUtil;
import com.example.carte.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.java.Log;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService; // service hybride

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Operation(summary = "Connexion utilisateur", description = "Authentifie l'utilisateur et retourne un JWT avec une durée de validité définie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "423", description = "Compte bloqué")
    })
    @PostMapping("/login")
    public JwtResponse<LoginDTO> login(@RequestBody AuthRequest req) {

        try {
            boolean passwordValid = userService.verifyUser(
                    req.getEmail(),
                    req.getPassword());

            if (!passwordValid) {
                return JwtResponse.error("Email ou mot de passe incorrect");
            }
            if(userService.isAccountLocked(req.getEmail())) {
                return JwtResponse.error("Compte bloqué en raison de trop nombreuses tentatives de connexion échouées");
            }
            UserDTO userDTO = userService.getUserByEmail(req.getEmail());
            String token = jwtUtil.generateToken(req.getEmail());

            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(userDTO.getEmail());
            loginDTO.setRole(userDTO.getRole());
            loginDTO.setToken(token);

            return JwtResponse.success(loginDTO);

        } catch (Exception e) {
            return JwtResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "Débloquer un utilisateur", description = "Réinitialise le nombre de tentatives échouées et débloque le compte")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur débloqué"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping("/unlock/{email}")
    public ResponseEntity<Void> unlockUser(
            @PathVariable @Parameter(description = "Email de l'utilisateur à débloquer", example = "user@mail.com") String email) {

        userService.resetLoginAttempts(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login2")
    public ResponseEntity<LoginDTO> login2(@RequestBody AuthRequest request) {
        try {
            // Utilisation du service hybride
            System.out.println("UserDTO: " + request.getEmail() + ", " + request.getFirebaseUid());
            var userDTO = userService.getUserHybrid(request.getFirebaseUid(), request.getEmail(),
                    request.getPassword());
            // Vérification mot de passe offline uniquement
            if (!userService.checkPasswordLocal(userDTO.getEmail(), request.getPassword())) {
                User newUser = new User();
                newUser.setFirebaseUid(request.getFirebaseUid());
                newUser.setEmail(request.getEmail()); // ou request.getEmail() si disponible
                // Hasher le mot de passe pour stockage local
                newUser.setPassword(request.getPassword());
                newUser.setRole("USER");
                // Assigner un profil par défaut
                newUser.setProfil(userService.getdefaultProfil());

                // Sauvegarder l'utilisateur localement
                userService.saveUser(newUser);

                // userDTO = userService.toDTO(newUser);

            }
            if (!userService.isOnline() && !userService.checkPasswordLocal(userDTO.getEmail(), request.getPassword())) {
                return ResponseEntity.status(401).body(null);
            }

            // Génération du token JWT local
            String jwtToken = jwtUtil.generateToken(userDTO.getEmail());

            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(userDTO.getEmail());
            loginDTO.setRole(userDTO.getRole());
            loginDTO.setToken(jwtToken);

            return ResponseEntity.ok(loginDTO);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @Operation(summary = "Inscription utilisateur", description = "Enregistre un nouvel utilisateur et retourne un JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscription réussie"),
            @ApiResponse(responseCode = "400", description = "Erreur lors de l'inscription")
    })
    @PostMapping("/register")
    public JwtResponse<?> register(@RequestBody AuthRegisterRequest request) {
        try {
            System.out.println("Registering user with email: " + request.getEmail());
            userService.enregistrerUser(request.getEmail(), request.getPassword(), request.getRole());

            return JwtResponse.success("Utilisateur enregistré avec succès");

        } catch (Exception e) {
            return JwtResponse.error("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

}
