package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.dto.SignalementDTO;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.TypeSignalement;
import com.example.carte.entities.User;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.TypeSignalementRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepo;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    private TypeSignalementRepository typeSignalementRepository;

    @Autowired
    private ProblemeService problemeService;

    @Autowired
    private TypeSignalementService typeSignalementService;

    @Autowired
    private UserService userService;
    public SignalementService(SignalementRepository signalementRepo, UtilisateurRepository utilisateurRepository) {
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public List<SignalementDTO> getListSyncSignalements() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("signalements");

        List<Signalement> localSignalements = signalementRepo.findAll();

        if (isOnline()) {
            Map<String, Signalement> localByFirebaseId = localSignalements.stream()
                    .filter(s -> s.getFirebaseId() != null && !s.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(Signalement::getFirebaseId, s -> s));

            List<SignalementDTO> firebaseList = colRef.get().get().getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTO)
                    .collect(Collectors.toList());

            Map<String, SignalementDTO> firebaseById = firebaseList.stream()
                    .filter(dto -> dto.getFirebaseId() != null && !dto.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(SignalementDTO::getFirebaseId, dto -> dto));

            for (SignalementDTO dto : firebaseList) {
                String fbId = dto.getFirebaseId();
                if (fbId == null || fbId.isBlank())
                    continue;

                Signalement existingLocal = signalementRepo.findByFirebaseId(fbId).orElse(null);
                System.out.println("Syncing Firebase signalement fbId=" + fbId + " with local "
                        + (existingLocal != null ? "id " + existingLocal.getIdSignalement() : "new entry"));
                if (existingLocal != null) {
                    existingLocal.setLatitude(dto.getLatitude());
                    existingLocal.setLongitude(dto.getLongitude());
                    existingLocal.setSurfaceM2(dto.getSurfaceM2());
                    existingLocal.setDateSignalement(dto.getDateSignalement());
                    signalementRepo.save(existingLocal);
                    localByFirebaseId.put(fbId, existingLocal); // mettre à jour map
                } else {
                    Signalement newLocal = mapDTOToEntity(dto);
                    if (dto.getCompteEmail() != null) {
                        User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                                .orElseThrow(() -> new RuntimeException(
                                        "Compte introuvable pour email: " + dto.getCompteEmail()));
                        newLocal.setCompte(compte);
                    }
                    signalementRepo.save(newLocal);
                    localByFirebaseId.put(fbId, newLocal);
                }
            }
            // localSignalements = new ArrayList<>(localByFirebaseId.values());

            System.out.println("Local signalements after Firebase sync: " + localByFirebaseId.size());
            for (Signalement local : localSignalements) {
                String fbId = local.getFirebaseId();
                if (fbId == null || fbId.isBlank()) {
                    fbId = db.collection("signalements").document().getId();
                    local.setFirebaseId(fbId);
                    signalementRepo.save(local);
                    System.out.println(
                            "Generated new fbId for local signalement id " + local.getIdSignalement() + ": " + fbId);
                }
                Map<String, Object> signalementMap = new HashMap<>();
                signalementMap.put("idSignalement", local.getIdSignalement());
                signalementMap.put("dateSignalement", local.getDateSignalement().toString());
                signalementMap.put("longitude", local.getLongitude());
                signalementMap.put("latitude", local.getLatitude());
                signalementMap.put("surfaceM2", local.getSurfaceM2());
                signalementMap.put("description", local.getDescription());
                signalementMap.put("firebaseId", fbId);
                if( local.getTypeSignalement().getFirebaseId()==null){
                    typeSignalementService.syncer();
                }
                if(local.getCompte().getFirebaseUid()==null){
                    userService.syncCompteLocalToFirebase(local.getCompte());
                }
                //syncer le compte
                signalementMap.put("idTypeSignalement", local.getTypeSignalement().getFirebaseId());
                signalementMap.put("compteEmail", local.getCompte() != null ? local.getCompte().getEmail() : null);
                signalementMap.put("idCompte", local.getCompte().getFirebaseUid());
                db.collection("signalements").document(fbId).set(signalementMap);
                //si le typeSignalement ne possede pas de fbid on le sync vers firebase 
            }

            return signalementRepo.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Fallback local si offline ou erreur
        return localSignalements.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SignalementDTO> getAllSignalements() {
        if (isOnline()) {
            try {
                Firestore db = FirestoreClient.getFirestore();
                CollectionReference colRef = db.collection("signalements");

                // 1️⃣ Récupérer tous les signalements depuis Firebase
                List<SignalementDTO> firebaseList = colRef.get().get().getDocuments()
                        .stream()
                        .map(this::mapFirestoreToDTO)
                        .collect(Collectors.toList());

                // Créer un map pour lookup rapide par firebaseId
                Map<String, SignalementDTO> firebaseMap = firebaseList.stream()
                        .filter(dto -> dto.getFirebaseId() != null)
                        .collect(Collectors.toMap(SignalementDTO::getFirebaseId, dto -> dto));

                // 2️⃣ Synchronisation Firebase → local
                for (SignalementDTO dto : firebaseList) {
                    if (dto.getFirebaseId() == null)
                        continue;

                    signalementRepo.findByFirebaseId(dto.getFirebaseId())
                            .orElseGet(() -> {
                                Signalement s = mapDTOToEntity(dto);

                                // Récupérer le compte associé
                                if (dto.getCompteEmail() != null) {
                                    User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                                            .orElseThrow(() -> new RuntimeException(
                                                    "Compte introuvable pour email: " + dto.getCompteEmail()));
                                    s.setCompte(compte);
                                }
                                return signalementRepo.save(s);
                            });
                }

                // 3️⃣ Récupérer tous les signalements locaux
                List<Signalement> localSignalements = signalementRepo.findAll();

                // 4️ Synchronisation local → Firebase
                for (Signalement local : localSignalements) {
                    String fbId = local.getFirebaseId();

                    if (fbId == null || !firebaseMap.containsKey(fbId)) {

                        // Générer ID si nécessaire
                        if (fbId == null || fbId.isBlank()) {
                            fbId = db.collection("signalements").document().getId();
                            local.setFirebaseId(fbId);
                            // save() ici seulement si on modifie local
                            signalementRepo.save(local);
                        }

                        // Préparer les données pour Firestore
                        Map<String, Object> signalementMap = new HashMap<>();
                        signalementMap.put("idSignalement", local.getIdSignalement());
                        signalementMap.put("dateSignalement", local.getDateSignalement().toString());
                        signalementMap.put("longitude", local.getLongitude());
                        signalementMap.put("latitude", local.getLatitude());
                        signalementMap.put("surfaceM2", local.getSurfaceM2());
                        signalementMap.put("firebaseId", fbId);
                        signalementMap.put("compteEmail",
                                local.getCompte() != null ? local.getCompte().getEmail() : null);

                        db.collection("signalements").document(fbId).set(signalementMap);

                        SignalementDTO dto = mapToDTO(local);
                        dto.setFirebaseId(fbId);
                        firebaseMap.put(fbId, dto);
                    }
                }

                // Retourner la liste finale
                return new ArrayList<>(firebaseMap.values());

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Firebase inaccessible, fallback local");
            }
        }

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
        dto.setIdTypeSignalement(s.getTypeSignalement().getIdType());
        if (s.getProbleme() != null) {
            ProblemeDTO pDto = problemeService.mapToDTO(s.getProbleme());
            dto.setProblemeDTO(pDto);
        }
        return dto;
    }

    /** Mapping DTO → Entity pour sauvegarde locale */
    private Signalement mapDTOToEntity(SignalementDTO dto) {
        Signalement s = new Signalement();
        s.setFirebaseId(dto.getFirebaseId());
        s.setDateSignalement(dto.getDateSignalement());
        s.setLatitude(dto.getLatitude());
        s.setLongitude(dto.getLongitude());
        s.setSurfaceM2(dto.getSurfaceM2());
        s.setDescription(dto.getDescription());
        TypeSignalement type = typeSignalementRepository.findById(dto.getIdTypeSignalement()).get();
        s.setTypeSignalement(type);
        // Compte à résoudre via email localement
        // s.setCompte(userRepo.findByEmail(dto.getCompteEmail()).orElseThrow(...));
        return s;
    }
}
