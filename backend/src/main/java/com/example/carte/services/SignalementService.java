package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.dto.SignalementDTO;
import com.example.carte.dto.UserDTO;
import com.example.carte.entities.Probleme;
import com.example.carte.entities.ProblemeStatus;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.SignalementImage;
import com.example.carte.entities.TypeSignalement;
import com.example.carte.entities.User;
import com.example.carte.repository.ProblemeStatusRepository;
import com.example.carte.repository.SignalementImageRepository;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.TypeSignalementRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.firebase.auth.FirebaseAuthException;
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

    @Autowired
    private SignalementImageRepository signalementImageRepository;
    @Autowired
    private ProblemeStatusRepository problemeStatusRepository;
    @Autowired
    private ConfigurationService configurationService;

    private final ReentrantLock syncLock = new ReentrantLock();

    public SignalementService(SignalementRepository signalementRepo, UtilisateurRepository utilisateurRepository) {
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void syncer() throws Exception {
        this.getListSyncSignalements();
    }

    @Transactional
    public List<SignalementDTO> getListSyncSignalements() throws Exception {
        syncLock.lock();
        try {
            System.out.println("🔍 ========== DEBUT SYNC SIGNALEMENTS ========== " + LocalDateTime.now());

            Firestore db = FirestoreClient.getFirestore();
            CollectionReference colRef = db.collection("signalements");

            // 1️⃣ Récupérer tous les signalements locaux
            List<Signalement> localSignalements = signalementRepo.findAll();
            System.out.println("🔍 Nombre de signalements locaux: " + localSignalements.size());

            if (!isOnline()) {
                return localSignalements.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }

            System.out.println("en ligne " + isOnline());

            // Sync type signalement d'abord
            typeSignalementService.syncer();

            // Sync comptes si nécessaire
            Set<User> comptesToSync = localSignalements.stream()
                    .filter(s -> s.getCompte() != null &&
                            (s.getCompte().getFirebaseUid() == null ||
                                    s.getCompte().getFirebaseUid().isBlank()))
                    .map(Signalement::getCompte)
                    .collect(Collectors.toSet());

            if (!comptesToSync.isEmpty()) {
                System.out.println("🔄 Synchronisation de " + comptesToSync.size() + " COMPTES...");
                for (User compte : comptesToSync) {
                    try {
                        userService.syncCompteLocalToFirebase(compte);
                    } catch (Exception e) {
                        System.err.println("Erreur sync compte: " + e.getMessage());
                    }
                }
            }

            // 🔑 CACHE pour tracker les firebaseId déjà traités
            Map<String, Signalement> processedSignalements = new HashMap<>();

            // 2️⃣ SYNC LOCAL → FIREBASE
            System.out.println("🔍 === PHASE LOCAL → FIREBASE ===");
            for (Signalement local : localSignalements) {
                String fbId = local.getFirebaseId();
                System.out.println("🔍 Traitement Signalement ID=" + local.getIdSignalement() + ", firebaseId=" + fbId);

                if (fbId == null || fbId.isBlank()) {
                    // ➕ CREATE Firebase
                    fbId = colRef.document().getId();
                    System.out.println("🔍   ➕ CREATE Firebase avec nouveau fbId: " + fbId);
                    local.setFirebaseId(fbId);
                    local.setLastSync(LocalDateTime.now());

                    // Créer le signalement dans Firebase
                    Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                    colRef.document(fbId).set(signalementMap);

                    signalementRepo.save(local);

                    // 📸 SYNC images LOCAL → FIREBASE
                    syncImagesLocalToFirebase(db, local, fbId);

                    processedSignalements.put(fbId, local);
                    System.out.println("🔍   ✅ Créé et ajouté au cache");
                    continue;
                }

                // fbId existe → vérifier Firebase
                DocumentSnapshot firebaseDoc = colRef.document(fbId).get().get();

                if (!firebaseDoc.exists()) {
                    // 🔁 Firebase manquant → CREATE
                    System.out.println("🔍   🔁 Firebase manquant, CREATE");
                    local.setLastSync(LocalDateTime.now());

                    Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                    colRef.document(fbId).set(signalementMap);

                    signalementRepo.save(local);

                    // 📸 SYNC images LOCAL → FIREBASE
                    syncImagesLocalToFirebase(db, local, fbId);

                    processedSignalements.put(fbId, local);
                    System.out.println("🔍   ✅ Créé et ajouté au cache");
                    continue;
                }

                // Firebase existe → comparer lastSync
                LocalDateTime firebaseLastSync = parseFirebaseTimestamp(
                        firebaseDoc.getString("lastSync"));

                System.out.println(
                        "🔍   Firebase lastSync: " + firebaseLastSync + ", Local lastSync: " + local.getLastSync());

                if (local.getLastSync() != null &&
                        (firebaseLastSync == null ||
                                local.getLastSync().isAfter(firebaseLastSync))) {

                    // ⬆️ Local plus récent → UPDATE Firebase
                    System.out.println("🔍   ⬆️ Local plus récent, UPDATE Firebase");
                    local.setLastSync(LocalDateTime.now());

                    Map<String, Object> signalementMap = buildFirebaseMap(local, fbId);
                    colRef.document(fbId).set(signalementMap);

                    signalementRepo.save(local);

                    // 📸 SYNC images LOCAL → FIREBASE
                    syncImagesLocalToFirebase(db, local, fbId);
                } else {
                    System.out.println("🔍   ⏭️ Firebase à jour, skip");
                }

                processedSignalements.put(fbId, local);
                System.out.println("🔍   ✅ Ajouté au cache");
            }

            System.out.println("🔍 Cache size après LOCAL→FIREBASE: " + processedSignalements.size());

            // 3️⃣ SYNC FIREBASE → LOCAL
            System.out.println("🔍 === PHASE FIREBASE → LOCAL ===");
            List<SignalementDTO> firebaseList = colRef.get().get()
                    .getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTO)
                    .collect(Collectors.toList());

            System.out.println("🔍 Nombre de signalements Firebase: " + firebaseList.size());

            for (SignalementDTO firebaseDto : firebaseList) {
                String fbId = firebaseDto.getFirebaseId();
                System.out.println("🔍 Traitement Firebase fbId: " + fbId);

                if (fbId == null || fbId.isBlank()) {
                    System.out.println("🔍   ⚠️ fbId vide, skip");
                    continue;
                }

                if (firebaseDto.getLastSync() == null)
                    firebaseDto.setLastSync(firebaseDto.getDateSignalement());
                // 🔑 VÉRIFIER D'ABORD LE CACHE
                if (processedSignalements.containsKey(fbId)) {
                    System.out.println("🔍   ✅ Déjà dans le cache, vérification lastSync");
                    Signalement existingLocal = processedSignalements.get(fbId);
                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    !firebaseDto.getLastSync().isBefore(existingLocal.getLastSync()))) {

                        System.out.println("🔍   ⬇️ Firebase plus récent, UPDATE local");
                        updateLocalFromFirebase(existingLocal, firebaseDto);
                        signalementRepo.save(existingLocal);

                        // 📸 SYNC images FIREBASE → LOCAL
                        syncImagesFirebaseToLocal(db, existingLocal, fbId);
                    } else {
                        System.out.println("🔍   ⏭️ Local à jour, skip");
                    }
                    continue;
                }

                // Chercher dans la base de données
                System.out.println("🔍   ⚠️ PAS dans le cache, recherche en base");
                Optional<Signalement> localOpt = signalementRepo.findByFirebaseId(fbId);

                if (localOpt.isPresent()) {
                    System.out.println("🔍   ✅ Trouvé en base");
                    Signalement existingLocal = localOpt.get();

                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    !firebaseDto.getLastSync().isBefore(existingLocal.getLastSync()))) {

                        System.out.println("🔍   ⬇️ Firebase plus récent, UPDATE local");
                        updateLocalFromFirebase(existingLocal, firebaseDto);
                        signalementRepo.save(existingLocal);

                        // 📸 SYNC images FIREBASE → LOCAL
                        syncImagesFirebaseToLocal(db, existingLocal, fbId);
                    }

                } else {
                    // ➕ Nouveau signalement → CREATE local
                    System.out.println("🔍   ➕ NOUVEAU signalement, CREATE local");
                    Signalement newLocal = mapDTOToEntity(firebaseDto);
                    newLocal.setFirebaseId(fbId);
                    newLocal.setLastSync(firebaseDto.getLastSync());

                    if (firebaseDto.getCompteEmail() != null) {
                        User compte = utilisateurRepository.findByEmail(firebaseDto.getCompteEmail())
                                .orElse(null);
                        if (compte != null) {
                            newLocal.setCompte(compte);
                        }
                    }

                    // Sauvegarder le signalement d'abord
                    newLocal = signalementRepo.save(newLocal);
                    System.out.println("🔍   ✅ Signalement créé en local");

                    // 📸 SYNC images FIREBASE → LOCAL
                    syncImagesFirebaseToLocal(db, newLocal, fbId);
                }
            }

            System.out.println("🔍 ========== FIN SYNC SIGNALEMENTS ========== " + LocalDateTime.now());

            // 4️⃣ Retourner la liste locale finale
            return signalementRepo.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } finally {
            syncLock.unlock();
        }
    }

    // ========== MÉTHODES UTILITAIRES POUR LES IMAGES ==========

    /**
     * Synchronise les images LOCAL → FIREBASE
     */
    private void syncImagesLocalToFirebase(Firestore db, Signalement local, String signalementFbId)
            throws ExecutionException, InterruptedException {

        if (local.getImages() == null || local.getImages().isEmpty()) {
            System.out.println("🔍     📸 Pas d'images à synchroniser");
            return;
        }

        System.out.println("🔍     📸 Synchronisation de " + local.getImages().size() + " images vers Firebase");

        CollectionReference imagesCol = db.collection("signalements")
                .document(signalementFbId)
                .collection("images");

        for (SignalementImage img : local.getImages()) {
            String imgFbId = img.getFirebaseId();

            if (imgFbId == null || imgFbId.isBlank()) {
                // Générer un nouveau firebaseId pour l'image
                imgFbId = imagesCol.document().getId();
                img.setFirebaseId(imgFbId);
                System.out.println("🔍       ➕ Nouvelle image fbId: " + imgFbId);
            }

            // Préparer les données de l'image
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("content", img.getLien());
            imageMap.put("firebaseId", imgFbId);
            imageMap.put("createdAt", LocalDateTime.now().toString());

            // Enregistrer dans Firebase
            imagesCol.document(imgFbId).set(imageMap);

            // Sauvegarder localement
            signalementImageRepository.save(img);
            System.out.println("🔍       ✅ Image synchronisée: " + imgFbId);
        }
    }

    /**
     * Synchronise les images FIREBASE → LOCAL
     */
    private void syncImagesFirebaseToLocal(Firestore db, Signalement local, String signalementFbId)
            throws ExecutionException, InterruptedException {

        System.out.println("🔍     📸 Récupération des images depuis Firebase");

        CollectionReference imagesCol = db.collection("signalements")
                .document(signalementFbId)
                .collection("images");

        List<QueryDocumentSnapshot> imageDocs = imagesCol.get().get().getDocuments();
        System.out.println("🔍     📸 " + imageDocs.size() + " images trouvées dans Firebase");

        for (QueryDocumentSnapshot imgDoc : imageDocs) {
            String imgFbId = imgDoc.getId();
            // String content = imgDoc.getString("content");
            Object contentObj = imgDoc.get("content");
            String content = contentObj != null ? contentObj.toString() : null;
            // Chercher l'image localement par firebaseId
            Optional<SignalementImage> localImgOpt = signalementImageRepository.findByFirebaseId(imgFbId);
            if (content == null || content.isEmpty()) {
                System.out.println("🔍       ⚠️ Content vide, skip");
                continue;// sa skip
            }
            if (localImgOpt.isPresent()) {
                // UPDATE image existante
                SignalementImage localImg = localImgOpt.get();
                localImg.setLien(content);
                localImg.setSignalement(local);
                localImg.setLastSync(LocalDateTime.now());
                signalementImageRepository.save(localImg);
                System.out.println("🔍       ⬇️ Image mise à jour: " + imgFbId);
            } else {
                // CREATE nouvelle image
                SignalementImage newImg = new SignalementImage();
                newImg.setFirebaseId(imgFbId);
                newImg.setLien(content);
                newImg.setSignalement(local);
                newImg.setLastSync(LocalDateTime.now());
                signalementImageRepository.save(newImg);
                System.out.println("🔍       ➕ Nouvelle image créée: " + imgFbId);
            }
        }
    }

    // Autres méthodes utilitaires restent identiques...

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
        dto.setDescription(doc.getString("description"));
        // Gestion safe de la date - handle both Timestamp and String formats
        try {
            // Try to get as Timestamp first (correct Firestore type)
            com.google.cloud.Timestamp fbTimestamp = doc.getTimestamp("dateSignalement");
            if (fbTimestamp != null) {
                dto.setDateSignalement(
                        fbTimestamp
                                .toDate()
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime());
            } else {
                dto.setDateSignalement(LocalDateTime.now());
            }
        } catch (Exception e) {
            // Fallback for legacy string format
            try {
                String dateStr = doc.getString("dateSignalement");
                if (dateStr != null) {
                    dto.setDateSignalement(LocalDateTime.parse(dateStr));
                } else {
                    dto.setDateSignalement(LocalDateTime.now());
                }
            } catch (Exception ex) {
                dto.setDateSignalement(LocalDateTime.now());
            }
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
        List<SignalementImage> signalementImages = signalementImageRepository.findBySignalement_IdSignalement(s.getIdSignalement());
        System.out.println("idddddddddddddddddddddd "+s.getIdSignalement()+" "+signalementImages.size());
        List<String> liens = new ArrayList<>();
        if(!signalementImages.isEmpty()){
            //on ajoute au dto
            for (SignalementImage si : signalementImages) {
                liens.add(si.getLien());
            }
        }
        
        dto.setLienImage(liens);
        String entrepriseNom = null;
        if (s.getProbleme() != null && s.getProbleme().getEntreprise() != null) {
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
     * @throws FirebaseAuthException 
     */
    private Signalement mapDTOToEntity(SignalementDTO dto) throws InterruptedException, ExecutionException, FirebaseAuthException {
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
        double budget = configurationService.calculerBudget(probleme.getNiveau());
        dto.setBudget(budget);
        dto.setEntrepriseNom(probleme.getEntreprise() != null ? probleme.getEntreprise().getNom() : null);
        dto.setIdEntreprise(probleme.getEntreprise() != null ? probleme.getEntreprise().getFirebaseId() : null);

        dto.setCompteEmail(probleme.getCompte() != null ? probleme.getCompte().getEmail() : null);
        dto.setSignalementId(probleme.getSignalement() != null ? probleme.getSignalement().getIdSignalement() : null);
        ProblemeStatus latestStatus = problemeStatusRepository.findTopByProblemeOrderByDateStatusDesc(probleme);
        System.out.println("In signalement Service, Status taken: idStatus=" + latestStatus.getStatus().getIdStatus() + ", nom=" + latestStatus.getStatus().getNom());
        dto.setStatut(latestStatus.getStatus().getIdStatus());
        dto.setStatutNom(latestStatus.getStatus().getNom());
        return dto;
    }
}
