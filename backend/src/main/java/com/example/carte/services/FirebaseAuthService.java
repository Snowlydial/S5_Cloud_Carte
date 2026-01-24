package com.example.carte.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FirebaseAuthService {

    @Value("${firebase.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verifyPassword(String email, String password) {

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
        
        System.out.println("Firebase API Key loaded: " + (apiKey != null && !apiKey.isEmpty()));
        System.out.println("Attempting Firebase authentication for: " + email);

        Map<String, Object> body = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        try {
            restTemplate.postForObject(url, body, String.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid credentials");
            // return false;
        }
    }
}

