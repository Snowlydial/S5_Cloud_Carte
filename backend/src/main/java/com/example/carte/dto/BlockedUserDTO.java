package com.example.carte.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Utilisateur bloqué")
public class BlockedUserDTO {

    @Schema(example = "12")
    private Integer id;

    @Schema(example = "user@email.com")
    private String email;

    @Schema(example = "MANAGER")
    private String role;

    @Schema(description = "Nombre de tentatives échouées", example = "4")
    private int loginAttempts;

    // getters & setters
}
