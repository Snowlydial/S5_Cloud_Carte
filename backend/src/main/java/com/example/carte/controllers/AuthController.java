package com.example.carte.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.LoginDTO;
import com.example.carte.request.AuthRequest;
import com.example.carte.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String jwtToken = jwtUtil.generateToken(request.getUsername());

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(request.getUsername());
        // loginDTO.setMotDePasse(request.getPassword());
        loginDTO.setToken(jwtToken);

        return ResponseEntity.ok(loginDTO);
    }

}
