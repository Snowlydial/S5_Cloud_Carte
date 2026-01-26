package com.example.carte.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.carte.dto.BlockedUserDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.services.UserService;

import io.swagger.v3.oas.annotations.Operation;

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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Lister les utilisateurs bloqués")
    @GetMapping("/blocked")
    public ResponseEntity<List<BlockedUserDTO>> getBlockedUsers() {
        return ResponseEntity.ok(userService.getBlockedUsers());
    }
}
