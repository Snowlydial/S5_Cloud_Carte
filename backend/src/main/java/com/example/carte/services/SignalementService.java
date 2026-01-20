package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.carte.dto.SignalementDTO;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.User;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepo;
    private final UtilisateurRepository utilisateurRepository;

    public SignalementService(SignalementRepository signalementRepo, UtilisateurRepository utilisateurRepository) {
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<SignalementDTO> getAllSignalements() {
        if (isOnline()) {
            try {
                CollectionReference colRef = FirestoreClient.getFirestore().collection("signalements");

                List<SignalementDTO> firebaseList = colRef.get().get().getDocuments()
                        .stream()
                        .map(this::mapFirestoreToDTO)
                        .collect(Collectors.toList());

                for (SignalementDTO dto : firebaseList) {
                    signalementRepo.findByFirebaseId(dto.getFirebaseId())
                            .orElseGet(() -> {
                                // Récupération du compte (exemple via email dans le DTO)
                                User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                                        .orElseThrow(() -> new RuntimeException(
                                                "Compte introuvable pour email: " + dto.getCompteEmail()));

                                // Map DTO → Entity
                                Signalement signalement = mapDTOToEntity(dto);

                                // Associer le compte
                                signalement.setCompte(compte);

                                // Sauvegarder
                                return signalementRepo.save(signalement);
                            });
                }

                List<SignalementDTO> localList = signalementRepo.findAll()
                        .stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());

                for (Signalement signalement : signalementRepo.findAll()) {
                    boolean existsInFirebase = firebaseList.stream()
                            .anyMatch(fbDto -> fbDto.getFirebaseId() != null &&
                                    fbDto.getFirebaseId().equals(signalement.getFirebaseId()));

                    if (!existsInFirebase) {
                        SignalementDTO dto = mapToDTO(signalement);
                        String docId = signalement.getFirebaseId();
                        if (docId == null || docId.isBlank()) {
                            docId = FirestoreClient.getFirestore().collection("signalements").document().getId();
                        }
                        Map<String, Object> signalementMap = new HashMap<>();
                        signalementMap.put("idSignalement", dto.getIdSignalement());
                        signalementMap.put("dateSignalement", dto.getDateSignalement().toString()); // ISO String
                        signalementMap.put("longitude", dto.getLongitude());
                        signalementMap.put("latitude", dto.getLatitude());
                        signalementMap.put("surfaceM2", dto.getSurfaceM2());
                        signalementMap.put("firebaseId", dto.getFirebaseId());
                        signalementMap.put("compteEmail", dto.getCompteEmail());
                        FirestoreClient.getFirestore()
                                .collection("signalements")
                                .document(docId)
                                .set(signalementMap);
                        System.out.println("Ajouté à Firebase: " + dto.getFirebaseId());
                        firebaseList.add(dto);
                    }
                }

                // Ajouter les DTO locaux manquants
                for (SignalementDTO localDto : localList) {
                    boolean existsInFirebase = firebaseList.stream()
                            .anyMatch(fbDto -> fbDto.getFirebaseId().equals(localDto.getFirebaseId()));
                    if (!existsInFirebase) {
                        FirestoreClient.getFirestore()
                                .collection("signalements")
                                .document(localDto.getFirebaseId())
                                .set(localDto);
                        firebaseList.add(localDto);
                    }
                }

                return firebaseList;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Firebase inaccessible, fallback local");
            }
        }

        // Fallback local
        return signalementRepo.findAll()
                .stream()
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
        dto.setCompteEmail(s.getCompte().getEmail());
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
        // Compte à résoudre via email localement
        // s.setCompte(userRepo.findByEmail(dto.getCompteEmail()).orElseThrow(...));
        return s;
    }
}
