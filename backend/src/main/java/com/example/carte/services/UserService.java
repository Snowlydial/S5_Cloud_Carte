package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.carte.dto.UserDTO;
import com.example.carte.entities.Profil;
import com.example.carte.entities.User;
import com.example.carte.repository.ProfilRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.example.carte.request.AuthRegisterRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Service
public class UserService {

    private final UtilisateurRepository userRepo;
    private final ProfilRepository profilRepository;

    public UserService(UtilisateurRepository userRepo, ProfilRepository profilRepository) {
        this.userRepo = userRepo;
        this.profilRepository = profilRepository;
    }

    // Login hybride
    public User login(String email, String password) {
        if (isOnline()) {
            // Login via Firebase
            try {
                FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(password);
                String firebaseUid = token.getUid();
                return syncFirebaseUser(firebaseUid, email);
            } catch (Exception e) {
                System.out.println("Firebase inaccessible, fallback offline");
            }
        }

        // Fallback offline
        return userRepo.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Login failed"));
    }

    // Synchroniser Firebase -> local
    private User syncFirebaseUser(String firebaseUid, String email) {
        User user = userRepo.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setFirebaseUid(firebaseUid);
                    return u;
                });
        user.setLastSync(LocalDateTime.now());
        return userRepo.save(user);
    }

    // Méthode simple de détection d'internet
    public boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    public UserDTO getUserByFirebaseUid(String firebaseUid) {
        User user = userRepo.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (Firebase UID)"));
        return mapToDTO(user);
    }

    /** Récupère un utilisateur par email (local) */
    public UserDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (email)"));
        return mapToDTO(user);
    }

    /** Mapping User -> UserDTO */
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setFirebaseUid(user.getFirebaseUid());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setLastSync(user.getLastSync());
        return dto;
    }

    public UserDTO getUserHybrid(String firebaseUid, String email) {
        if (isOnline()) {
            try {
                FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(firebaseUid);
                return getUserByFirebaseUid(token.getUid());
            } catch (Exception e) {
                // fallback local
                System.out.println("Firebase offline, fallback local");
            }
        }

        // fallback offline
        return getUserByEmail(email);
    }

    public boolean checkPasswordLocal(String email, String password) {
        return userRepo.findByEmail(email)
                .map(user -> user.getPassword() != null && user.getPassword().equals(password))
                .orElse(false);
    }

    public UserDTO registerUser(AuthRegisterRequest request) throws FirebaseAuthException {

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Utilisateur déjà existant");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // offline
        user.setRole("USER");
        Profil profil = profilRepository.findByNom("USER")
                .orElseThrow(() -> new RuntimeException("Profil par défaut introuvable"));
        user.setProfil(profil);

        if (isOnline() && request.getFirebaseUid() != null && !request.getFirebaseUid().isEmpty()) {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseUid());
            String firebaseUid = token.getUid();
            // Assigner Firebase UID et synchroniser
            user.setFirebaseUid(firebaseUid);
            user = syncFirebaseUser(user.getFirebaseUid(), user.getEmail());
        }

        // Toujours sauvegarder localement
        User saved = userRepo.save(user);

        return mapToDTO(saved);
    }
}
