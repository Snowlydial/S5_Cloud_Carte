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

    public Profil getdefaultProfil() {
        Optional<Profil> profilOpt = profilRepository.findByNom("MANAGER");
        return profilOpt.orElseThrow(() -> new RuntimeException("Profil par défaut introuvable"));
    }

    public void saveUser(User user) {
        userRepo.save(user);
    }

    // Login hybride
    public User login(String email, String password) {
        if (isOnline()) {
            // Login via Firebase
            try {
                FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(password);
                String firebaseUid = token.getUid();
                return syncFirebaseUser(firebaseUid, email, password);
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
    private User syncFirebaseUser(String firebaseUid, String email, String password) {
        User user = userRepo.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setFirebaseUid(firebaseUid);
                    u.setRole("USER");

                    // Profil par défaut
                    Profil profil = profilRepository.findByNom("USER")
                            .orElseThrow(() -> new RuntimeException("Profil par défaut introuvable"));
                    u.setProfil(profil);

                    // Mot de passe temporaire ou vide (offline)
                    u.setPassword(password); // ou null si accepté
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
        System.out.println("Getting user by email: " + email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (email)"));
        // jerenea ilay isana tentatives de connexion

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

    public UserDTO getUserHybrid(String firebaseUid, String email, String password) {
        System.out.println("Getting user hybrid: " + firebaseUid + ", " + email);
        if (isOnline()) {
            System.out.println("System is online, trying Firebase UID");
            try {
                // FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(firebaseUid);
                // System.out.println("Firebase token verified for UID: " + token.getUid());
                User u = userRepo.findByFirebaseUid(firebaseUid).get();
                System.out.println("Firebase online, user found: " + u.getEmail());
                if (u.getIsBlocked()) {
                    throw new Exception("user bloque");
                }
                if (u.getPassword() != password && u.getLoginAttempts() <= 3) {
                    int tentatives = u.getLoginAttempts();
                    tentatives++;
                    u.setLoginAttempts(tentatives);
                }
                if (u.getLoginAttempts() > 3) {
                    u.setIsBlocked(true);
                }
                return getUserByFirebaseUid(firebaseUid);
            } catch (Exception e) {
                // fallback local
                System.out.println("Firebase offline, fallback local");
            }
        }
        System.out.println("System is offline or Firebase failed, using email");
        // fallback offline
        try {
            User u = userRepo.findByEmail(email).get();
            if (u.getIsBlocked()) {
                throw new Exception("user bloque");
            }
            if (u.getPassword() != password && u.getLoginAttempts() <= 3) {
                int tentatives = u.getLoginAttempts();
                tentatives++;
                u.setLoginAttempts(tentatives);
            }
            if (u.getLoginAttempts() > 3) {
                u.setIsBlocked(true);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
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
        Profil profil = profilRepository.findByNom("MANAGER")
                .orElseThrow(() -> new RuntimeException("Profil par défaut introuvable"));
        user.setProfil(profil);
        System.out.println("Registering user: " + request.getFirebaseUid());
        if (isOnline() && request.getFirebaseUid() != null && !request.getFirebaseUid().isEmpty()) {
            try {
                System.out.println("Verifying Firebase UID: " + request.getFirebaseUid());
                FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseUid());
                System.out.println("Firebase token verified for UID: " + token.getUid());
                String firebaseUid = token.getUid();
                user.setFirebaseUid(firebaseUid);
                user = syncFirebaseUser(user.getFirebaseUid(), user.getEmail(), user.getPassword());
            } catch (FirebaseAuthException e) {
                throw new RuntimeException("Erreur de vérification Firebase", e);
            }
        }

        // Toujours sauvegarder localement
        User saved = userRepo.save(user);

        return mapToDTO(saved);
    }
}
