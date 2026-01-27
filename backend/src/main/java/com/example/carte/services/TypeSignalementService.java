package com.example.carte.services;

import com.example.carte.dto.TypeSignalementDTO;
import com.example.carte.entities.TypeSignalement;
import com.example.carte.repository.TypeSignalementRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TypeSignalementService {

    private final TypeSignalementRepository typeSignalementRepo;

    public TypeSignalementService(TypeSignalementRepository typeSignalementRepo) {
        this.typeSignalementRepo = typeSignalementRepo;
    }
    void syncer() throws InterruptedException, ExecutionException{
        List<TypeSignalementDTO> dto = this.getListSyncTypeSignalements();
    }
    @Transactional
    public List<TypeSignalementDTO> getListSyncTypeSignalements() throws InterruptedException, ExecutionException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("type_signalement");

        List<TypeSignalement> localTypes = typeSignalementRepo.findAll();

        if (isOnline()) {
            // Map local par firebaseId
            Map<String, TypeSignalement> localByFirebaseId = localTypes.stream()
                    .filter(t -> t.getFirebaseId() != null && !t.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(TypeSignalement::getFirebaseId, t -> t));

            // Liste Firestore
            List<TypeSignalementDTO> firebaseList = colRef.get().get().getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTOTypeSignalement)
                    .collect(Collectors.toList());

            Map<String, TypeSignalementDTO> firebaseById = firebaseList.stream()
                    .filter(dto -> dto.getFirebaseId() != null && !dto.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(TypeSignalementDTO::getFirebaseId, dto -> dto));

            // Synchroniser Firestore vers local
            for (TypeSignalementDTO dto : firebaseList) {
                String fbId = dto.getFirebaseId();
                if (fbId == null || fbId.isBlank())
                    continue;

                TypeSignalement existingLocal = typeSignalementRepo.findByFirebaseId(fbId).orElse(null);
                System.out.println("Syncing Firebase typeSignalement fbId=" + fbId + " with local "
                        + (existingLocal != null ? "id " + existingLocal.getIdType() : "new entry"));

                if (existingLocal != null) {
                    existingLocal.setNom(dto.getNom());
                    typeSignalementRepo.save(existingLocal);
                    localByFirebaseId.put(fbId, existingLocal);
                } else {
                    TypeSignalement newLocal = mapDTOToEntityTypeSignalement(dto);
                    typeSignalementRepo.save(newLocal);
                    localByFirebaseId.put(fbId, newLocal);
                }
            }

            // Synchroniser local vers Firestore
            for (TypeSignalement local : localTypes) {
                String fbId = local.getFirebaseId();
                if (fbId == null || fbId.isBlank()) {
                    fbId = db.collection("type_signalement").document().getId();
                    local.setFirebaseId(fbId);
                    typeSignalementRepo.save(local);
                    System.out.println(
                            "Generated new fbId for local typeSignalement id " + local.getIdType() + ": " + fbId);
                }

                Map<String, Object> typeMap = new HashMap<>();
                typeMap.put("idType", local.getIdType());
                typeMap.put("nom", local.getNom());
                typeMap.put("firebaseId", fbId);

                db.collection("type_signalement").document(fbId).set(typeMap);
            }

            return typeSignalementRepo.findAll().stream()
                    .map(this::mapToDTOTypeSignalement)
                    .collect(Collectors.toList());
        }

        // Fallback local si offline ou erreur
        return localTypes.stream()
                .map(this::mapToDTOTypeSignalement)
                .collect(Collectors.toList());
    }

    private TypeSignalementDTO mapFirestoreToDTOTypeSignalement(DocumentSnapshot doc) {
        if (doc == null || !doc.exists())
            return null;

        TypeSignalementDTO dto = new TypeSignalementDTO();
        dto.setIdType(doc.contains("idType") ? doc.getLong("idType").intValue() : null);
        dto.setNom(doc.contains("nom") ? doc.getString("nom") : null);
        dto.setFirebaseId(doc.contains("firebaseId") ? doc.getString("firebaseId") : null);

        return dto;
    }

    private TypeSignalement mapDTOToEntityTypeSignalement(TypeSignalementDTO dto) {
        if (dto == null)
            return null;

        TypeSignalement entity = new TypeSignalement();
        entity.setIdType(dto.getIdType());
        entity.setNom(dto.getNom());
        entity.setFirebaseId(dto.getFirebaseId());

        return entity;
    }

    private TypeSignalementDTO mapToDTOTypeSignalement(TypeSignalement entity) {
        if (entity == null)
            return null;

        TypeSignalementDTO dto = new TypeSignalementDTO();
        dto.setIdType(entity.getIdType());
        dto.setNom(entity.getNom());
        dto.setFirebaseId(entity.getFirebaseId());

        return dto;
    }
     private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

}
