package com.example.carte.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String motDePasse;
    private String token;
}
