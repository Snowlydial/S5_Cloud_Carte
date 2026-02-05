package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.dto.SignalementDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.Probleme;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.TypeSignalement;
import com.example.carte.entities.User;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.TypeSignalementRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.transaction.Transactional;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepo;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    private TypeSignalementRepository typeSignalementRepository;

    // @Autowired
    // private ProblemeService problemeService;

    @Autowired
    private TypeSignalementService typeSignalementService;

    @Autowired
    private UserService userService;

    private final ReentrantLock syncLock = new ReentrantLock();

    public SignalementService(SignalementRepository signalementRepo, UtilisateurRepository utilisateurRepository) {
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
    }
    public void syncer() throws Exception{
        this.getListSyncSignalements();
    }

    // @Transactional
    // public List<SignalementDTO> getListSyncSignalements() throws Exception {
    // Firestore db = FirestoreClient.getFirestore();
    // CollectionReference colRef = db.collection("signalements");

    // List<Signalement> localSignalements = signalementRepo.findAll();

    // if (isOnline()) {
    // Map<String, Signalement> localByFirebaseId = localSignalements.stream()
    // .filter(s -> s.getFirebaseId() != null && !s.getFirebaseId().isBlank())
    // .collect(Collectors.toMap(Signalement::getFirebaseId, s -> s));

    // List<SignalementDTO> firebaseList = colRef.get().get().getDocuments()
    // .stream()
    // .map(this::mapFirestoreToDTO)
    // .collect(Collectors.toList());

    // Map<String, SignalementDTO> firebaseById = firebaseList.stream()
    // .filter(dto -> dto.getFirebaseId() != null && !dto.getFirebaseId().isBlank())
    // .collect(Collectors.toMap(SignalementDTO::getFirebaseId, dto -> dto));

    // for (SignalementDTO dto : firebaseList) {

    // String fbId = dto.getFirebaseId();
    // if (fbId == null || fbId.isBlank())
    // continue;

    // Signalement existingLocal =
    // signalementRepo.findByFirebaseId(fbId).orElse(null);
    // System.out.println("Syncing Firebase signalement fbId=" + fbId + " with local
    // "
    // + (existingLocal != null ? "id " + existingLocal.getIdSignalement() : "new
    // entry"));
    // if (existingLocal != null) {
    // existingLocal.setLatitude(dto.getLatitude());
    // existingLocal.setLongitude(dto.getLongitude());
    // existingLocal.setSurfaceM2(dto.getSurfaceM2());
    // existingLocal.setDateSignalement(dto.getDateSignalement());
    // signalementRepo.save(existingLocal);
    // localByFirebaseId.put(fbId, existingLocal); // mettre à jour map
    // } else {
    // Signalement newLocal = mapDTOToEntity(dto);
    // if (dto.getCompteEmail() != null) {
    // User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
    // .orElseThrow(() -> new RuntimeException(
    // "Compte introuvable pour email: " + dto.getCompteEmail()));
    // newLocal.setCompte(compte);
    // }
    // signalementRepo.save(newLocal);
    // localByFirebaseId.put(fbId, newLocal);
    // }
    // }
    // // localSignalements = new ArrayList<>(localByFirebaseId.values());

    // System.out.println("Local signalements after Firebase sync: " +
    // localByFirebaseId.size());
    // for (Signalement local : localSignalements) {
    // String fbId = local.getFirebaseId();
    // if (fbId == null || fbId.isBlank()) {
    // fbId = db.collection("signalements").document().getId();
    // local.setFirebaseId(fbId);
    // signalementRepo.save(local);
    // System.out.println(
    // "Generated new fbId for local signalement id " + local.getIdSignalement() +
    // ": " + fbId);
    // }
    // Map<String, Object> signalementMap = new HashMap<>();
    // signalementMap.put("idSignalement", local.getIdSignalement());
    // signalementMap.put("dateSignalement", local.getDateSignalement().toString());
    // signalementMap.put("longitude", local.getLongitude());
    // signalementMap.put("latitude", local.getLatitude());
    // signalementMap.put("surfaceM2", local.getSurfaceM2());
    // signalementMap.put("description", local.getDescription());
    // signalementMap.put("firebaseId", fbId);
    // if (local.getTypeSignalement().getFirebaseId() == null) {
    // typeSignalementService.syncer();
    // }
    // if (local.getCompte().getFirebaseUid() == null) {
    // userService.syncCompteLocalToFirebase(local.getCompte());
    // }
    // // syncer le compte
    // signalementMap.put("idTypeSignalement",
    // local.getTypeSignalement().getFirebaseId());
    // signalementMap.put("compteEmail", local.getCompte() != null ?
    // local.getCompte().getEmail() : null);
    // signalementMap.put("idCompte", local.getCompte().getFirebaseUid());
    // db.collection("signalements").document(fbId).set(signalementMap);
    // // si le typeSignalement ne possede pas de fbid on le sync vers firebase
    // }

    // return signalementRepo.findAll().stream()
    // .map(this::mapToDTO)
    // .collect(Collectors.toList());
    // }

    // // Fallback local si offline ou erreur
    // return localSignalements.stream()
    // .map(this::mapToDTO)
    // .collect(Collectors.toList());
    // }
    @Transactional
    public List<SignalementDTO> getListSyncSignalements() throws Exception {
        syncLock.lock();
        try {
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference colRef = db.collection("signalements");

            // 1️⃣ Récupérer tous les signalements locaux
            List<Signalement> localSignalements = signalementRepo.findAll();

            if (!isOnline()) {
                return localSignalements.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }

            System.out.println("en ligne " + isOnline());
            typeSignalementService.syncer();

            // 🔑 CACHE pour tracker les firebaseId déjà traités
            Map<String, Signalement> processedSignalements = new HashMap<>();

            // 2️⃣ SYNC LOCAL → FIREBASE D'ABORD
            for (Signalement local : localSignalements) {
                String fbId = local.getFirebaseId();

                if (fbId == null || fbId.isBlank()) {
                    // Pas encore de Firebase ID → CREATE dans Firebase
                    fbId = db.collection("signalements").document().getId();
                    local.setFirebaseId(fbId);
                    local.setLastSync(LocalDateTime.now());

                    // Synchroniser le compte si nécessaire
                    if (local.getCompte() != null &&
                            (local.getCompte().getFirebaseUid() == null ||
                                    local.getCompte().getFirebaseUid().isBlank())) {
                        userService.syncCompteLocalToFirebase(local.getCompte());
                    }

                    // Créer dans Firebase
                    Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                    db.collection("signalements").document(fbId).set(signalementMap);

                    signalementRepo.save(local);

                    // 🔑 Ajouter au cache
                    processedSignalements.put(fbId, local);

                } else {
                    // Firebase ID existe → vérifier si UPDATE nécessaire
                    DocumentSnapshot firebaseDoc = db.collection("signalements")
                            .document(fbId)
                            .get()
                            .get();

                    if (firebaseDoc.exists()) {
                        LocalDateTime firebaseLastSync = parseFirebaseTimestamp(
                                firebaseDoc.getString("lastSync"));

                        // Local plus récent que Firebase ?
                        if (local.getLastSync() != null &&
                                (firebaseLastSync == null ||
                                        local.getLastSync().isAfter(firebaseLastSync))) {

                            // Local est plus récent → UPDATE Firebase
                            local.setLastSync(LocalDateTime.now());

                            if (local.getCompte() != null &&
                                    (local.getCompte().getFirebaseUid() == null ||
                                            local.getCompte().getFirebaseUid().isBlank())) {
                                userService.syncCompteLocalToFirebase(local.getCompte());
                            }

                            Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                            db.collection("signalements").document(fbId).set(signalementMap);

                            signalementRepo.save(local);
                        }

                    } else {
                        // Document n'existe pas dans Firebase → CREATE
                        local.setLastSync(LocalDateTime.now());

                        if (local.getCompte() != null &&
                                (local.getCompte().getFirebaseUid() == null ||
                                        local.getCompte().getFirebaseUid().isBlank())) {
                            userService.syncCompteLocalToFirebase(local.getCompte());
                        }

                        Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                        db.collection("signalements").document(fbId).set(signalementMap);

                        signalementRepo.save(local);
                    }

                    // 🔑 Ajouter au cache
                    processedSignalements.put(fbId, local);
                }
            }

            // 3️⃣ SYNC FIREBASE → LOCAL (APRÈS avoir pushé le local)
            // Récupérer à nouveau les données Firebase (maintenant à jour avec nos pushs)
            List<SignalementDTO> firebaseList = colRef.get().get()
                    .getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTO)
                    .collect(Collectors.toList());

            for (SignalementDTO firebaseDto : firebaseList) {
                String fbId = firebaseDto.getFirebaseId();
                if (fbId == null || fbId.isBlank()) {
                    continue;
                }

                // 🔑 VÉRIFIER D'ABORD LE CACHE
                if (processedSignalements.containsKey(fbId)) {
                    // Ce signalement a déjà été traité dans LOCAL → FIREBASE
                    // On vérifie juste si Firebase a une version plus récente
                    Signalement existingLocal = processedSignalements.get(fbId);

                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    firebaseDto.getLastSync().isAfter(existingLocal.getLastSync()))) {
                        // Firebase est plus récent → UPDATE local
                        updateLocalFromFirebase(existingLocal, firebaseDto);
                        signalementRepo.save(existingLocal);
                    }
                    continue; // ⚠️ NE PAS créer de doublon
                }

                // Chercher dans la base de données (pour les signalements d'autres
                // utilisateurs)
                Optional<Signalement> localOpt = signalementRepo.findByFirebaseId(fbId);

                if (localOpt.isPresent()) {
                    // L'instance existe localement
                    Signalement existingLocal = localOpt.get();

                    // Comparer les timestamps (Firebase plus récent ?)
                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    firebaseDto.getLastSync().isAfter(existingLocal.getLastSync()))) {

                        // Firebase est plus récent → UPDATE local
                        updateLocalFromFirebase(existingLocal, firebaseDto);
                        signalementRepo.save(existingLocal);
                    }

                } else {
                    // Nouveau signalement créé par un autre utilisateur → CREATE local
                    Signalement newLocal = mapDTOToEntity(firebaseDto);
                    newLocal.setFirebaseId(fbId);
                    newLocal.setLastSync(firebaseDto.getLastSync());

                    if (firebaseDto.getCompteEmail() != null) {
                        User compte = utilisateurRepository.findByEmail(firebaseDto.getCompteEmail())
                                .orElseThrow(() -> new RuntimeException(
                                        "Compte introuvable pour email: " + firebaseDto.getCompteEmail()));
                        newLocal.setCompte(compte);
                    }

                    signalementRepo.save(newLocal);
                }
            }

            // 4️⃣ Retourner la liste locale finale
            return signalementRepo.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } finally {
            syncLock.unlock();
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void updateLocalFromFirebase(Signalement local, SignalementDTO firebaseDto) {
        local.setLatitude(firebaseDto.getLatitude());
        local.setLongitude(firebaseDto.getLongitude());
        local.setSurfaceM2(firebaseDto.getSurfaceM2());
        local.setDateSignalement(firebaseDto.getDateSignalement());
        local.setDescription(firebaseDto.getDescription());
        local.setLastSync(firebaseDto.getLastSync());
        local.setDescription(firebaseDto.getDescription());
        if (firebaseDto.getIdTypeSignalement() != null) {
            TypeSignalement type = typeSignalementRepository
                    .findByFirebaseId(firebaseDto.getIdTypeSignalement())
                    .orElse(null);
            if (type != null) {
                local.setTypeSignalement(type);
            }
        }
    }

    // Méthodes utilitaires

    private Map<String, Object> buildFirebaseMap(Signalement local, String fbId) {
        Map<String, Object> map = new HashMap<>();
        map.put("idSignalement", local.getIdSignalement());
        map.put("dateSignalement", local.getDateSignalement().toString());
        map.put("longitude", local.getLongitude());
        map.put("latitude", local.getLatitude());
        map.put("surfaceM2", local.getSurfaceM2());
        map.put("description", local.getDescription());
        map.put("firebaseId", fbId);
        map.put("lastSync", local.getLastSync().toString());
        map.put("idTypeSignalement",
                local.getTypeSignalement() != null ? local.getTypeSignalement().getFirebaseId() : null);
        map.put("compteEmail",
                local.getCompte() != null ? local.getCompte().getEmail() : null);
        map.put("idCompte",
                local.getCompte() != null ? local.getCompte().getFirebaseUid() : null);

        return map;
    }

    private LocalDateTime parseFirebaseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public List<SignalementDTO> getAllSignalements() {

        // Fallback local si offline ou erreur
        return signalementRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Vérifie si internet/Firebase est accessible
    private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    /** Mapping Firestore → DTO */
    private SignalementDTO mapFirestoreToDTO(QueryDocumentSnapshot doc) {
        SignalementDTO dto = new SignalementDTO();
        dto.setFirebaseId(doc.getId());
        dto.setIdTypeSignalement(doc.getString("idTypeSignalement"));
        dto.setIdCompte(doc.getString("idCompte"));
        // Gestion safe de la date
        if (doc.getTimestamp("date_signalement") != null) {
            dto.setDateSignalement(
                    doc.getTimestamp("date_signalement")
                            .toDate()
                            .toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime());
        } else {
            dto.setDateSignalement(LocalDateTime.now()); // ou LocalDateTime.now() si tu veux une valeur par défaut
        }

        dto.setLatitude(doc.getDouble("latitude"));
        dto.setLongitude(doc.getDouble("longitude"));
        dto.setSurfaceM2(doc.getDouble("surfaceM2"));
        dto.setCompteEmail(doc.getString("compteEmail"));
        return dto;
    }

    /** Mapping Entity → DTO */
    private SignalementDTO mapToDTO(Signalement s) {
        SignalementDTO dto = new SignalementDTO();
        dto.setIdSignalement(s.getIdSignalement());
        dto.setDateSignalement(s.getDateSignalement());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setSurfaceM2(s.getSurfaceM2());
        dto.setFirebaseId(s.getFirebaseId());
        dto.setDescription(s.getDescription());
        dto.setCompteEmail(s.getCompte().getEmail());
        dto.setIdTypeSignalement(s.getTypeSignalement().getFirebaseId());
        String entrepriseNom = null;
        if (s.getProbleme() != null && s.getProbleme().getEntreprise()!=null) {
            entrepriseNom = s.getProbleme().getEntreprise().getNom();
        }
        dto.setEntreprise(entrepriseNom);
        if (s.getProbleme() != null) {
            ProblemeDTO pDto = this.mapToDTO(s.getProbleme());
            dto.setProblemeDTO(pDto);
        }
        return dto;
    }

    /**
     * Mapping DTO → Entity pour sauvegarde locale
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Signalement mapDTOToEntity(SignalementDTO dto) throws InterruptedException, ExecutionException {
        System.out.println("dto " + dto.toString());
        Signalement s = new Signalement();
        User u = utilisateurRepository.findByFirebaseUid(dto.getFirebaseId()).orElse(null);
        s.setFirebaseId(dto.getFirebaseId());
        s.setDateSignalement(dto.getDateSignalement());
        s.setLatitude(dto.getLatitude());
        s.setLongitude(dto.getLongitude());
        s.setSurfaceM2(dto.getSurfaceM2());
        s.setDescription(dto.getDescription());
        if (s.getTypeSignalement() == null || s.getTypeSignalement().getFirebaseId() == null) {
            typeSignalementService.syncer();
            if (dto.getFirebaseId() != null) {
                System.out.println(";lsdvnsbbbbbbbbbb --------------------------------> ");
                TypeSignalement typeLocal = typeSignalementRepository.findByFirebaseId(dto.getIdTypeSignalement())
                        .orElseThrow(() -> new RuntimeException(
                                "TypeSignalement local introuvable pour fbId: " + dto.getIdTypeSignalement()));
                dto.setIdTypeSignalement(typeLocal.getFirebaseId());
            }
        }
        if (s.getCompte() == null || s.getCompte().getFirebaseUid() == null) {
            List<UserDTO> users = userService.getListSyncComptes();
            u = utilisateurRepository.findByFirebaseUid(dto.getIdCompte()).orElseThrow();
        }
        System.out.println("dto " + dto.toString());

        System.out.println("sunc " + dto.getIdTypeSignalement());
        TypeSignalement type = typeSignalementRepository.findByFirebaseId(dto.getIdTypeSignalement())
                .orElseThrow(() -> new RuntimeException(
                        "TypeSignalement local introuvable pour fbId: " + dto.getIdTypeSignalement()));
        s.setTypeSignalement(type);
        s.setCompte(u);
        // Compte à résoudre via email localement
        // s.setCompte(userRepo.findByEmail(dto.getCompteEmail()).orElseThrow(...));
        return s;
    }
        public ProblemeDTO mapToDTO(Probleme probleme) {
        ProblemeDTO dto = new ProblemeDTO();
        dto.setIdProbleme(probleme.getIdProbleme());
        dto.setDateProbleme(probleme.getDateProbleme());
        dto.setSurfaceM2(probleme.getSurfaceM2());
        dto.setBudget(probleme.getBudget());
        dto.setEntrepriseNom(probleme.getEntreprise() != null ? probleme.getEntreprise().getIdEntreprise() : null);
        dto.setCompteEmail(probleme.getCompte() != null ? probleme.getCompte().getEmail() : null);
        dto.setSignalementId(probleme.getSignalement() != null ? probleme.getSignalement().getIdSignalement() : null);
        dto.setStatut(probleme.getStatusList().getLast().getStatus().getIdStatus());
        if(probleme.getStatusList().getLast()!=null){
            
            dto.setStatutNom(probleme.getStatusList().getLast().getStatus().getNom());
        }
        return dto;
    }
}
