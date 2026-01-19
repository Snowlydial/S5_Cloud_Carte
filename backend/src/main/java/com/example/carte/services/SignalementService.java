package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.carte.dto.SignalementDTO;
import com.example.carte.entities.Signalement;
import com.example.carte.repository.SignalementRepository;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepo;

    public SignalementService(SignalementRepository signalementRepo) {
        this.signalementRepo = signalementRepo;
    }


    public List<SignalementDTO> getAllSignalements() {
        if (isOnline()) {
            try {
                // Firestore
                CollectionReference colRef = FirestoreClient.getFirestore().collection("signalements");
                List<SignalementDTO> firebaseList = colRef.get().get().getDocuments()
                        .stream()
                        .map(this::mapFirestoreToDTO)
                        .collect(Collectors.toList());

                // Synchronisation locale
                for (SignalementDTO dto : firebaseList) {
                    signalementRepo.findByFirebaseId(dto.getFirebaseId())
                            .orElseGet(() -> signalementRepo.save(mapDTOToEntity(dto)));
                }

                return firebaseList;
            } catch (Exception e) {
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
        dto.setDateSignalement(doc.getTimestamp("date_signalement").toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        dto.setLatitude(doc.getDouble("latitude"));
        dto.setLongitude(doc.getDouble("longitude"));
        dto.setSurfaceM2(doc.getDouble("surfaceM2"));
        // Le compte peut être géré par email ou firebaseUid
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
