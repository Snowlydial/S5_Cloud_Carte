package com.example.carte.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.carte.repository.UtilisateurRepository;

@Controller
@CrossOrigin(origins = "*")
public class LoginController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // correspond à src/main/resources/templates/login.html
    }

    public LoginController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/login-form")
    public String loginForm(@RequestParam String username, @RequestParam String password, Model model) {
        // Vérification simple
        var userOpt = utilisateurRepository.findByEmail(username);
        if (userOpt.isPresent() && userOpt.get().getMotDePasse().equals(password)) {
            // Login réussi
            model.addAttribute("user", userOpt.get());
            return "welcome"; // redirige vers une page welcome.html
        } else {
            // Login échoué
            model.addAttribute("error", "Email ou mot de passe incorrect");
            return "login"; // reste sur la page login.html
        }
    }
}
