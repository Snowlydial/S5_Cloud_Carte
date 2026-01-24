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
import com.google.firebase.auth.UserRecord;

@Service
public class UserService {

    private final UtilisateurRepository userRepo;
    private final ProfilRepository profilRepository;
    private final FirebaseAuthService firebaseAuthService;

    public UserService(UtilisateurRepository userRepo, ProfilRepository profilRepository,
            FirebaseAuthService firebaseAuthService) {
        this.userRepo = userRepo;
        this.profilRepository = profilRepository;
        this.firebaseAuthService = firebaseAuthService;
    }

    // fonction pour reinitialiser les tentatives de connexion
    public void resetLoginAttempts(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLoginAttempts(0);
            user.setIsBlocked(false);
            userRepo.save(user);
        } else {
            throw new RuntimeException("Utilisateur non trouvé pour réinitialisation des tentatives de connexion");
        }
    }

    // fonction pour verifier un user
    // si c en ligne on verifie avec firebase
    // sinon on verifie en local
    public boolean verifyUser(String email, String password) {
        User u = userRepo.findByEmail(email).orElse(null);
        boolean accepted = false;
        try {
            if (isOnline()) {
                accepted = firebaseAuthService.verifyPassword(email, password);

            } else {
                // verifier le password en local
                accepted = checkPasswordLocal(email, password);
            }
            if (accepted) {
                return true;
            } else {
                System.out.println("User found for login attempts: " + (u != null));
                if (u != null) {
                    int tentatives = u.getLoginAttempts();
                    tentatives++;
                    u.setLoginAttempts(tentatives);
                    if (u.getLoginAttempts() > 3) {
                        u.setIsBlocked(true);
                    }
                    userRepo.save(u);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Firebase inaccessible, fallback offline");
        }
        return false;
    }

    public void enregistrerUser(String email, String password, String role) {
        // verifier si l'utilisateur existe deja
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            throw new RuntimeException("Utilisateur deja existant");
        }
        // enregistrer vers firebase si en ligne
        if (isOnline()) {
            try {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setPassword(password);
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                System.out.println("Successfully created new user: " + userRecord.getUid());

            } catch (FirebaseAuthException e) {
                throw new RuntimeException("Erreur lors de la création de l'utilisateur Firebase", e);
            }
        }
        // enregistrer localement
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);
        Profil profil = profilRepository.findByNom(role)
                .orElseThrow(() -> new RuntimeException("Profil introuvable pour le rôle: " + role));
        newUser.setProfil(profil);
        userRepo.save(newUser);
    }

    public boolean isAccountLocked(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getIsBlocked();
        } else {
            throw new RuntimeException("Utilisateur non trouvé pour vérification du compte bloqué");
        }
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
                if (!u.getPassword().equals(password) && u.getLoginAttempts() <= 3) {
                    int tentatives = u.getLoginAttempts();
                    tentatives++;
                    u.setLoginAttempts(tentatives);
                }
                if (u.getLoginAttempts() > 3) {
                    u.setIsBlocked(true);
                }
                userRepo.save(u);
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
            if (!u.getPassword().equals(password) && u.getLoginAttempts() <= 3) {
                int tentatives = u.getLoginAttempts();
                tentatives++;
                u.setLoginAttempts(tentatives);
            }
            if (u.getLoginAttempts() > 3) {
                u.setIsBlocked(true);
            }
            userRepo.save(u);

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
