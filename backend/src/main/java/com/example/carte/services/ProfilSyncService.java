package com.example.carte.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.ProfilDTO;
import com.example.carte.entities.Profil;
import com.example.carte.repository.ProfilRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class ProfilSyncService {

    @Autowired
    private ProfilRepository profilRepo;

    private boolean isOnline() {
        try {
            FirestoreClient.getFirestore().listCollections();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ---------- Mapping ---------- */

    public ProfilDTO mapFirestoreToDTOProfil(DocumentSnapshot doc) {
        ProfilDTO dto = new ProfilDTO();
        dto.setFirebaseId(doc.getId());
        dto.setNom(doc.getString("nom"));
        return dto;
    }

    public Profil mapDTOToEntityProfil(ProfilDTO dto) {
        Profil p = new Profil();
        p.setNom(dto.getNom());
        p.setFirebaseId(dto.getFirebaseId());
        p.setLastSync(LocalDateTime.now());
        return p;
    }

    public ProfilDTO mapToDTOProfil(Profil entity) {
        ProfilDTO dto = new ProfilDTO();
        dto.setIdProfil(entity.getIdProfil());
        dto.setNom(entity.getNom());
        dto.setFirebaseId(entity.getFirebaseId());
        return dto;
    }

    /* ---------- Synchronisation ---------- */

    public void syncProfil() throws Exception {
        if (!isOnline()) return;

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference col = db.collection("profil");

        QuerySnapshot snapshot = col.get().get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String firebaseId = doc.getId();

            if (profilRepo.findByFirebaseId(firebaseId)!=null) {
                ProfilDTO dto = mapFirestoreToDTOProfil(doc);
                Profil profil = mapDTOToEntityProfil(dto);
                profilRepo.save(profil);
            }
        }
    }
}

