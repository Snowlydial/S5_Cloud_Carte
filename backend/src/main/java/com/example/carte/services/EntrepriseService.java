package com.example.carte.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.EntrepriseDTO;
import com.example.carte.entities.Entreprise;
import com.example.carte.repository.EntrepriseRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

@Service
public class EntrepriseService {

    @Autowired
    EntrepriseRepository entrepriseRepository;

    @Transactional
    public List<EntrepriseDTO> getListSyncEntreprises()
            throws InterruptedException, ExecutionException {

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("entreprise");

        List<Entreprise> localEntreprises = entrepriseRepository.findAll();

        if (isOnline()) {

            Map<String, Entreprise> localByFirebaseId = localEntreprises.stream()
                    .filter(e -> e.getFirebaseId() != null && !e.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(Entreprise::getFirebaseId, e -> e));

            List<EntrepriseDTO> firebaseList = colRef.get().get().getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTOEntreprise)
                    .collect(Collectors.toList());

            for (EntrepriseDTO dto : firebaseList) {

                if (dto.getFirebaseId() == null || dto.getFirebaseId().isBlank())
                    continue;

                Entreprise local = entrepriseRepository
                        .findByFirebaseId(dto.getFirebaseId())
                        .orElse(null);

                if (local != null) {
                    // UPDATE local
                    local.setNom(dto.getNom());
                    entrepriseRepository.save(local);
                    localByFirebaseId.put(dto.getFirebaseId(), local);

                } else {
                    // CREATE local
                    Entreprise newLocal = mapDTOToEntityEntreprise(dto);
                    entrepriseRepository.save(newLocal);
                    localByFirebaseId.put(dto.getFirebaseId(), newLocal);
                }
            }

            // Sync local → Firebase
            for (Entreprise local : entrepriseRepository.findAll()) {

                String fbId = local.getFirebaseId();
                if (fbId == null || fbId.isBlank()) {
                    fbId = colRef.document().getId();
                    local.setFirebaseId(fbId);
                    entrepriseRepository.save(local);
                }

                Map<String, Object> map = new HashMap<>();
                map.put("idEntreprise", local.getIdEntreprise());
                map.put("nom", local.getNom());
                map.put("firebaseId", fbId);

                colRef.document(fbId).set(map);
            }

            return entrepriseRepository.findAll().stream()
                    .map(this::mapToDTOEntreprise)
                    .collect(Collectors.toList());
        }

        // Fallback offline
        return localEntreprises.stream()
                .map(this::mapToDTOEntreprise)
                .collect(Collectors.toList());
    }

    private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    private EntrepriseDTO mapFirestoreToDTOEntreprise(DocumentSnapshot doc) {
        EntrepriseDTO dto = new EntrepriseDTO();

        dto.setFirebaseId(doc.getId());
        dto.setIdEntreprise(doc.getLong("idEntreprise") != null
                ? doc.getLong("idEntreprise").intValue()
                : null);
        dto.setNom(doc.getString("nom"));

        return dto;
    }

    private Entreprise mapDTOToEntityEntreprise(EntrepriseDTO dto) {
        Entreprise entity = new Entreprise();

        entity.setIdEntreprise(dto.getIdEntreprise());
        entity.setNom(dto.getNom());
        entity.setFirebaseId(dto.getFirebaseId());

        return entity;
    }

    private EntrepriseDTO mapToDTOEntreprise(Entreprise entity) {
        EntrepriseDTO dto = new EntrepriseDTO();

        dto.setIdEntreprise(entity.getIdEntreprise());
        dto.setNom(entity.getNom());
        dto.setFirebaseId(entity.getFirebaseId());

        return dto;
    }

}
