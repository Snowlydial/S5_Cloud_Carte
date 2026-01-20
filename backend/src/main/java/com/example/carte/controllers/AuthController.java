package com.example.carte.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.LoginDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.User;
import com.example.carte.request.AuthRegisterRequest;
import com.example.carte.request.AuthRequest;
import com.example.carte.security.JwtUtil;
import com.example.carte.services.UserService;

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

    // @PostMapping("/login")
    // public ResponseEntity<LoginDTO> login(@RequestBody AuthRequest request) {

    // Authentication authentication = authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(request.getUsername(),
    // request.getPassword()));

    // String jwtToken = jwtUtil.generateToken(request.getUsername());

    // LoginDTO loginDTO = new LoginDTO();
    // loginDTO.setEmail(request.getUsername());
    // // loginDTO.setMotDePasse(request.getPassword());
    // loginDTO.setToken(jwtToken);

    // return ResponseEntity.ok(loginDTO);
    // }

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody AuthRequest request) {
        try {
            // Utilisation du service hybride
            System.out.println("UserDTO: " + request.getEmail() + ", " + request.getFirebaseUid());
            var userDTO = userService.getUserHybrid(request.getFirebaseUid(), request.getEmail(),request.getPassword());
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

    @PostMapping("/register")
    public ResponseEntity<LoginDTO> register(@RequestBody AuthRegisterRequest request) {
        try {
            System.out.println("Registering user with email: " + request.getEmail());
            UserDTO userDTO = userService.registerUser(request);

            // Générer JWT après inscription
            String jwtToken = jwtUtil.generateToken(userDTO.getEmail());

            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(userDTO.getEmail());
            loginDTO.setRole(userDTO.getRole());
            loginDTO.setToken(jwtToken);

            return ResponseEntity.ok(loginDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
