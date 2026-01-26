package com.example.carte.services;

import com.example.carte.entities.TypeSignalement;
import com.example.carte.repository.TypeSignalementRepository;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TypeSignalementService {

    private final TypeSignalementRepository typeSignalementRepo;

    public TypeSignalementService(TypeSignalementRepository typeSignalementRepo) {
        this.typeSignalementRepo = typeSignalementRepo;
    }

    /**
     * Synchronise tous les TypeSignalement locaux vers Firebase s'ils n'ont pas encore de firebaseId
     */
    @Transactional
    public void syncTypeSignalementsToFirebase() {
        Firestore db = FirestoreClient.getFirestore();

        List<TypeSignalement> localTypes = typeSignalementRepo.findAll();

        for (TypeSignalement type : localTypes) {
            // Vérifier si firebaseId existe
            if (type.getFirebaseId() == null || type.getFirebaseId().isBlank()) {
                String fbId = db.collection("type_signalements").document().getId();
                type.setFirebaseId(fbId);
                type.setLastSync(LocalDateTime.now());

                // Créer la map pour Firebase
                Map<String, Object> typeMap = new HashMap<>();
                typeMap.put("idType", type.getIdType());
                typeMap.put("nom", type.getNom());
                typeMap.put("firebaseId", fbId);
                typeMap.put("lastSync", type.getLastSync().toString());

                // Sauvegarder dans Firebase
                db.collection("type_signalements").document(fbId).set(typeMap);

                // Sauvegarder localement
                typeSignalementRepo.save(type);
            }
        }
    }
}
