package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.carte.dto.BlockedUserDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.Profil;
import com.example.carte.entities.User;
import com.example.carte.repository.ProfilRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.example.carte.request.AuthRegisterRequest;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

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
    // debloquer le user
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
        boolean existInFirebase = false;
        boolean existLocal = false;
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("compte");

        try {
            if (isOnline()) {
                // accepted = firebaseAuthService.verifyPassword(email, password);
                // existInFirebase = true;
                try {
                    accepted = firebaseAuthService.verifyPassword(email, password);
                    existInFirebase = true;
                } catch (Exception e) {
                    System.out.println("Firebase inaccessible, on continue en offline");
                }
            }
            if (checkPasswordLocal(email, password)) {
                // verifier le password en local
                accepted = checkPasswordLocal(email, password);
                existLocal = true;
            }
            if (accepted) {
                // synchronisation avec firebase si exist
                if (existInFirebase && !existLocal) {
                    // envoyer le firebase dans le local
                    syncCompteFirebaseToLocal(email);
                } else if (!existInFirebase && existLocal) {
                    // envoyer le local vers firebase
                    syncCompteLocalToFirebase(u);
                }
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

    public void syncCompteFirebaseToLocal(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("compte").document(email).get().get();

        if (!doc.exists())
            return;

        User user = new User();
        user.setEmail(email);
        user.setRole(doc.getString("role"));
        user.setFirebaseUid(doc.getString("firebaseUid"));
        user.setLoginAttempts(0);
        user.setIsBlocked(false);

        userRepo.save(user);
    }

    public void syncCompteLocalToFirebase(User u) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> compteMap = new HashMap<>();
        compteMap.put("email", u.getEmail());
        compteMap.put("role", u.getRole());
        compteMap.put("firebaseUid", u.getFirebaseUid());

        db.collection("compte").document(u.getEmail()).set(compteMap);
    }

    @Transactional
    public void enregistrerUser(String email, String password, String role) {
        // verifier si l'utilisateur existe deja
        Optional<User> userOpt = userRepo.findByEmail(email);
        String fbuid = null;
        System.out.println("kjhgfd");
        if (userOpt.isPresent()) {
            throw new RuntimeException("Utilisateur deja existant");
        }
        // enregistrer vers firebase si en ligne
        boolean firebaseUserExists = false;
        if (isOnline()) {

            try {
                FirebaseAuth.getInstance().getUserByEmail(email);
                firebaseUserExists = true;
            } catch (FirebaseAuthException e) {
                if (!"USER_NOT_FOUND".equals(e.getAuthErrorCode().name())) {
                    throw new RuntimeException("Erreur Firebase", e);
                }
            }
        }

        try {
            if (isOnline() && !firebaseUserExists) {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setPassword(password);
                System.out.println("lskvn");
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                System.out.println("Successfully created new user: " + userRecord.getUid());
                fbuid = userRecord.getUid();

            }
            // enregistrer localement
            System.out.println("lolllll  -----" + role);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setFirebaseUid(fbuid);
            Profil profil = profilRepository.findByNom("USER")
                    .orElseThrow(() -> new RuntimeException("Profil introuvable pour le rôle: " + role));
            newUser.setProfil(profil);
            userRepo.save(newUser);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de l'utilisateur Firebase", e);
        }
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

    public UserDTO getUserByEmail(String email) {
        System.out.println("Getting user by email: " + email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (email)"));
        // jerenea ilay isana tentatives de connexion

        return mapToDTO(user);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setFirebaseUid(user.getFirebaseUid());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getProfil().getNom());
        dto.setBlocked(user.getIsBlocked());
        dto.setTentative(user.getLoginAttempts());
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

    public List<BlockedUserDTO> getBlockedUsers() {

        return userRepo.findByIsBlockedTrue()
                .stream()
                .map(user -> {
                    BlockedUserDTO dto = new BlockedUserDTO();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole());
                    dto.setLoginAttempts(user.getLoginAttempts());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
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
