package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.carte.dto.BlockedUserDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.Profil;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.User;
import com.example.carte.repository.ProfilRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.example.carte.request.AuthRegisterRequest;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
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
    private final ProfilSyncService profilSyncService;

    public UserService(UtilisateurRepository userRepo, ProfilRepository profilRepository,
            FirebaseAuthService firebaseAuthService, ProfilSyncService profilSyncService) {
        this.userRepo = userRepo;
        this.profilRepository = profilRepository;
        this.firebaseAuthService = firebaseAuthService;
        this.profilSyncService = profilSyncService;
    }

    @Transactional
    public List<UserDTO> getListSyncComptes() throws InterruptedException, ExecutionException {

        Firestore db = FirestoreClient.getFirestore();

        List<User> localUsers = userRepo.findAll();

        if (!isOnline()) {
            return localUsers.stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        Map<String, User> localByFirebaseUid = localUsers.stream()
                .filter(u -> u.getFirebaseUid() != null && !u.getFirebaseUid().isBlank())
                .collect(Collectors.toMap(User::getFirebaseUid, u -> u));

        // Sync profiles first before syncing users (profiles are required as foreign
        // keys)
        try {
            profilSyncService.getListSyncProfils();
        } catch (Exception e) {
            System.out.println("Warning: Could not sync profiles: " + e.getMessage());
        }

        // alaina ireo avy am firebase
        CollectionReference colRef = db.collection("compte");
        List<UserDTO> firebaseUsers = colRef.get().get().getDocuments()
                .stream()
                .map(this::mapFirestoreToUserDTO)
                .toList();
        System.out.println("Firebase users fetched: " + firebaseUsers.size());
        for (UserDTO dto : firebaseUsers) {

            String fbUid = dto.getFirebaseUid();
            if (fbUid == null || fbUid.isBlank())
                continue;

            User local = localByFirebaseUid.get(fbUid);
            LocalDateTime fbLastSync = dto.getLastSync();

            if (local != null) {
                System.out.println("Syncing existing user: " + local.getEmail());
                if (isFirebaseNewer(fbLastSync, local.getLastSync())) {

                    local.setEmail(dto.getEmail());
                    local.setIsBlocked(dto.isBlocked());

                    if (dto.getTentative() != null) {
                        local.setLoginAttempts(dto.getTentative());
                    }

                    if (dto.getPassword() != null) {
                        local.setPassword(dto.getPassword());
                    }

                    Profil profil = profilRepository.findByNom(dto.getRole())
                            .orElseGet(this::getdefaultProfil);

                    if (profil.getFirebaseId() == null) {
                        profilSyncService.getListSyncProfils();
                    }
                    local.setProfil(profil);
                    local.setRole(profil.getNom());

                    local.setLastSync(fbLastSync);
                    userRepo.save(local);
                }

            } else {
                System.out.println("Existe pas en local");
                // Firebase existe mais pas en local
                User newUser = new User();
                newUser.setFirebaseUid(fbUid);
                newUser.setEmail(dto.getEmail());
                newUser.setIsBlocked(dto.isBlocked());
                newUser.setLoginAttempts(dto.getTentative());
                newUser.setPassword(dto.getPassword());

                Profil profil = profilRepository.findByNom(dto.getRole())
                        .orElseGet(this::getdefaultProfil);
                // if (profil.getFirebaseId() == null) {
                // profilSyncService.getListSyncProfils();
                // }
                newUser.setProfil(profil);
                newUser.setRole(profil.getNom());

                newUser.setLastSync(fbLastSync != null ? fbLastSync : LocalDateTime.now());
                userRepo.save(newUser);
                localByFirebaseUid.put(fbUid, newUser);
            }
        }

        // local makany am firebase
        for (User local : localUsers) {

            String fbId = db.collection("compte").document().getId();
            if (local.getFirebaseUid() == null || local.getFirebaseUid().isBlank()) {
                local.setFirebaseUid(fbId);
                userRepo.save(local);
            }

            DocumentReference docRef = colRef.document(local.getFirebaseUid());
            DocumentSnapshot snapshot = docRef.get().get();

            LocalDateTime fbLastSync = snapshot.exists()
                    ? parseDate(snapshot.getString("lastSync"))
                    : null;

            if (!snapshot.exists() || isLocalNewer(local.getLastSync(), fbLastSync)) {

                Map<String, Object> compteMap = new HashMap<>();
                compteMap.put("email", local.getEmail());
                compteMap.put("role", local.getProfil().getNom());
                compteMap.put("isBlocked", local.getIsBlocked());
                compteMap.put("tentative", local.getLoginAttempts());
                if (local.getProfil().getFirebaseId() == null) {
                    profilSyncService.getListSyncProfils();
                }
                compteMap.put("profil", local.getProfil().getFirebaseId());
                compteMap.put("firebaseUid", local.getFirebaseUid());
                compteMap.put("lastSync", local.getLastSync().toString());
                compteMap.put("password", local.getPassword());

                docRef.set(compteMap);
            }
        }

        return userRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private LocalDateTime parseDate(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }

    private boolean isFirebaseNewer(LocalDateTime fb, LocalDateTime local) {
        // If local is null, Firebase data should be pulled (local doesn't exist or
        // never synced)
        if (local == null)
            return true;
        // If Firebase lastSync is null but local exists, treat as equal (don't
        // overwrite)
        if (fb == null)
            return false;
        return fb.isAfter(local);
    }

    private boolean isLocalNewer(LocalDateTime local, LocalDateTime fb) {
        if (local == null)
            return false;
        if (fb == null)
            return true;
        return local.isAfter(fb);
    }

    // public void
    // fonction pour reinitialiser les tentatives de connexion
    // debloquer le user
    public void resetLoginAttempts(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("reset -----------------" + user.getEmail());
            user.setLoginAttempts(0);
            user.setIsBlocked(false);
            user.setLastSync(LocalDateTime.now());
            userRepo.save(user);
        } else {
            throw new RuntimeException("Utilisateur non trouvé pour réinitialisation des tentatives de connexion");
        }
    }

    // fonction pour verifier un user
    // si c en ligne on verifie avec firebase
    // sinon on verifie en local
    private UserDTO mapFirestoreToUserDTO(QueryDocumentSnapshot doc) {
        UserDTO dto = new UserDTO();

        dto.setFirebaseUid(doc.getId());

        dto.setEmail(doc.getString("email"));
        dto.setRole(doc.getString("role"));

        Boolean blocked = doc.getBoolean("isBlocked");
        dto.setBlocked(blocked != null ? blocked : false);
        dto.setPassword(doc.getString("password"));

        // Fetch tentative (login attempts) from Firestore
        Long tentative = doc.getLong("tentative");
        dto.setTentative(tentative != null ? tentative.intValue() : 0);
        Object lastSyncObj = doc.get("lastSync");
        if (lastSyncObj != null) {
            if (lastSyncObj instanceof com.google.cloud.Timestamp ts) {
                dto.setLastSync(
                        ts.toDate()
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime());
            } else if (lastSyncObj instanceof String str) {
                dto.setLastSync(LocalDateTime.parse(str)); // si c'est stocké en ISO
            } else {
                // fallback par défaut
                dto.setLastSync(LocalDateTime.now());
            }
        } else {
            dto.setLastSync(LocalDateTime.now());
        }

        return dto;
    }

    // public
    public boolean verifyUser(String email, String password) throws InterruptedException, ExecutionException {
        User u = userRepo.findByEmail(email).orElse(null);
        boolean accepted = false;
        boolean existInFirebase = false;
        boolean existLocal = false;
        List<UserDTO> users = this.getListSyncComptes();
        try {
            if (isOnline()) {
                Firestore db = FirestoreClient.getFirestore();
                // CollectionReference colRef = db.collection("compte");
                QuerySnapshot snapshot = db.collection("compte")
                        .whereEqualTo("email", email)
                        .get()
                        .get();
                // UserDTO firebaseUser = mapFirestoreToUserDTO(snapshot.getDocuments().get(0));
                if (snapshot.isEmpty()) {
                    syncCompteLocalToFirebase(u);
                }
                // User existingLocal =
                // userRepo.findByFirebaseUid(u.getFirebaseUid()).orElse(null);

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
        compteMap.put("tentative", u.getLoginAttempts());
        compteMap.put("isBlocked", u.getIsBlocked());
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
        if (profilOpt.isPresent()) {
            return profilOpt.get();
        }
        // If MANAGER profile doesn't exist, try to create it
        System.out.println("Creating default MANAGER profile as it doesn't exist");
        Profil defaultProfil = new Profil();
        defaultProfil.setNom("MANAGER");
        defaultProfil.setLastSync(LocalDateTime.now());
        return profilRepository.save(defaultProfil);
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
            System.out.println("isOnline check failed: " + e.getMessage());
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
        dto.setPassword(user.getPassword());
        dto.setBlocked(user.getIsBlocked());
        dto.setIdUser(user.getId());
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

    User getById(Integer id) {
        User u = userRepo.findById(id).orElseThrow();
        return u;
    }

    @Transactional
    public UserDTO updateUserEmail(Integer userId, String newEmail) throws FirebaseAuthException {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (userRepo.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        if (isOnline() && user.getFirebaseUid() != null && !user.getFirebaseUid().isBlank()) {
            try {
                // Met à jour l'email sur Firebase
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getFirebaseUid())
                        .setEmail(newEmail);
                FirebaseAuth.getInstance().updateUser(request);
                System.out.println("Firebase email updated for UID: " + user.getFirebaseUid());
            } catch (FirebaseAuthException e) {
                throw new RuntimeException("Erreur lors de la mise à jour Firebase", e);
            }
        }

        user.setEmail(newEmail);
        user.setLastSync(LocalDateTime.now());
        userRepo.save(user);

        if (isOnline() && user.getFirebaseUid() != null && !user.getFirebaseUid().isBlank()) {
            try {
                Firestore db = FirestoreClient.getFirestore();
                DocumentReference docRef = db.collection("compte").document(user.getFirebaseUid());

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("email", newEmail);
                updateMap.put("lastSync", LocalDateTime.now().toString());

                docRef.update(updateMap);
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour Firestore: " + e.getMessage());
            }
        }

        return mapToDTO(user);
    }

}
