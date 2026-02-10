package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.ProfilDTO;
import com.example.carte.entities.Profil;
import com.example.carte.repository.ProfilRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

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
    // @Transactional
    // public List<Profil> getListSyncProfils() throws InterruptedException,
    // ExecutionException {

    // Firestore db = FirestoreClient.getFirestore();
    // CollectionReference colRef = db.collection("profils");

    // List<Profil> localProfils = profilRepo.findAll();

    // if (!isOnline()) {
    // return localProfils;
    // }

    // // Map local par firebaseId
    // Map<String, Profil> localByFirebaseId = localProfils.stream()
    // .filter(p -> p.getFirebaseId() != null && !p.getFirebaseId().isBlank())
    // .collect(Collectors.toMap(Profil::getFirebaseId, p -> p));

    // /*
    // * =========================
    // * FIREBASE → LOCAL (PRIORITÉ)
    // * =========================
    // */
    // List<QueryDocumentSnapshot> firebaseDocs = colRef.get().get().getDocuments();

    // for (QueryDocumentSnapshot doc : firebaseDocs) {

    // String firebaseId = doc.getId();
    // String nom = doc.getString("nom");

    // if (nom == null || nom.isBlank())
    // continue;

    // Profil local = localByFirebaseId.get(firebaseId);

    // if (local != null) {
    // // Firebase gagne toujours sur le nom
    // if (!nom.equals(local.getNom())) {
    // local.setNom(nom);
    // }
    // local.setLastSync(LocalDateTime.now());
    // profilRepo.save(local);

    // } else {
    // // Nouveau profil depuis Firebase
    // Profil newProfil = new Profil();
    // newProfil.setFirebaseId(firebaseId);
    // newProfil.setNom(nom);
    // newProfil.setLastSync(LocalDateTime.now());

    // profilRepo.save(newProfil);
    // localByFirebaseId.put(firebaseId, newProfil);
    // }
    // }

    // /*
    // * =========================
    // * LOCAL → FIREBASE (SAFE)
    // * =========================
    // */
    // // Build a set of profile names that already exist in Firebase to avoid
    // duplicates
    // Map<String, String> firebaseNameToId = new HashMap<>();
    // for (QueryDocumentSnapshot doc : firebaseDocs) {
    // String nom = doc.getString("nom");
    // if (nom != null && !nom.isBlank()) {
    // firebaseNameToId.put(nom, doc.getId());
    // }
    // }

    // // Re-fetch local profiles to include any newly created ones
    // List<Profil> updatedLocalProfils = profilRepo.findAll();

    // for (Profil local : updatedLocalProfils) {

    // // Skip if this profile name already exists in Firebase
    // if (firebaseNameToId.containsKey(local.getNom())) {
    // // Update local firebaseId if not set
    // if (local.getFirebaseId() == null || local.getFirebaseId().isBlank()) {
    // local.setFirebaseId(firebaseNameToId.get(local.getNom()));
    // profilRepo.save(local);
    // }
    // continue;
    // }

    // if (local.getFirebaseId() == null || local.getFirebaseId().isBlank()) {
    // // UID logique basé sur le nom
    // local.setFirebaseId(local.getNom());
    // profilRepo.save(local);
    // }

    // DocumentReference docRef = colRef.document(local.getFirebaseId());
    // DocumentSnapshot snapshot = docRef.get().get();

    // if (!snapshot.exists()) {
    // Map<String, Object> profilMap = new HashMap<>();
    // profilMap.put("nom", local.getNom());
    // profilMap.put("firebaseId", local.getFirebaseId());
    // profilMap.put("lastSync", LocalDateTime.now().toString());

    // docRef.set(profilMap);
    // }
    // }

    // return profilRepo.findAll();
    // }
    @Transactional
    public List<Profil> getListSyncProfils() throws InterruptedException, ExecutionException {

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("profils");

        List<Profil> localProfils = profilRepo.findAll();

        if (!isOnline()) {
            return localProfils;
        }

        // Map local par nom pour comparaison
        Map<String, Profil> localByName = localProfils.stream()
                .filter(p -> p.getNom() != null && !p.getNom().isBlank())
                .collect(Collectors.toMap(Profil::getNom, p -> p));

        // =========================
        // FIREBASE → LOCAL (PRIORITÉ)
        // =========================
        List<QueryDocumentSnapshot> firebaseDocs = colRef.get().get().getDocuments();

        for (QueryDocumentSnapshot doc : firebaseDocs) {

            String firebaseId = doc.getId();
            String nom = doc.getString("nom");

            if (nom == null || nom.isBlank())
                continue;

            Profil local = localByName.get(nom);

            if (local != null) {
                // Firebase gagne toujours
                if (!firebaseId.equals(local.getFirebaseId())) {
                    local.setFirebaseId(firebaseId); // Met à jour l'UID si différent
                }
                local.setNom(nom); // Assure que le nom est identique
                local.setLastSync(LocalDateTime.now());
                profilRepo.save(local);

            } else {
                // Nouveau profil depuis Firebase
                Profil newProfil = new Profil();
                newProfil.setFirebaseId(firebaseId);
                newProfil.setNom(nom);
                newProfil.setLastSync(LocalDateTime.now());
                profilRepo.save(newProfil);

                localByName.put(nom, newProfil);
            }
        }

        // =========================
        // LOCAL → FIREBASE
        // =========================
        // Construire un map nom → firebaseId déjà existants dans Firebase
        Map<String, String> firebaseNameToId = firebaseDocs.stream()
                .filter(d -> d.getString("nom") != null)
                .collect(Collectors.toMap(d -> d.getString("nom"), QueryDocumentSnapshot::getId));

        for (Profil local : localProfils) {

            // Si le nom existe déjà dans Firebase, on met juste à jour le firebaseId local
            if (firebaseNameToId.containsKey(local.getNom())) {
                if (local.getFirebaseId() == null || local.getFirebaseId().isBlank()) {
                    local.setFirebaseId(firebaseNameToId.get(local.getNom()));
                    profilRepo.save(local);
                }
                continue;
            }

            // Sinon créer un document Firebase
            String fbUid = local.getFirebaseId() != null && !local.getFirebaseId().isBlank()
                    ? local.getFirebaseId()
                    : local.getNom(); // UID temporaire basé sur le nom si absent
            DocumentReference docRef = colRef.document(fbUid);

            Map<String, Object> profilMap = new HashMap<>();
            profilMap.put("nom", local.getNom());
            profilMap.put("firebaseId", fbUid);
            profilMap.put("lastSync", LocalDateTime.now().toString());

            docRef.set(profilMap);
            local.setFirebaseId(fbUid);
            profilRepo.save(local);
        }

        return profilRepo.findAll();
    }

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
        if (!isOnline())
            return;

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference col = db.collection("profil");

        QuerySnapshot snapshot = col.get().get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String firebaseId = doc.getId();

            if (profilRepo.findByFirebaseId(firebaseId) != null) {
                ProfilDTO dto = mapFirestoreToDTOProfil(doc);
                Profil profil = mapDTOToEntityProfil(dto);
                profilRepo.save(profil);
            }
        }
    }
}
