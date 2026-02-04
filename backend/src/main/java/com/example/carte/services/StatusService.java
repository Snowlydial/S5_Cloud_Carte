package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.example.carte.dto.StatusDTO;
import com.example.carte.entities.Status;
import com.example.carte.repository.StatutRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

@Service
public class StatusService {

    private final StatutRepository statusRepository;
    private final ReentrantLock syncLock = new ReentrantLock();

    public StatusService(StatutRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<StatusDTO> getAllStatus() {
        return statusRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private StatusDTO mapToDTO(Status status) {
        StatusDTO dto = new StatusDTO();
        dto.setIdStatus(status.getIdStatus());
        dto.setNom(status.getNom());
        return dto;
    }
    public boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public List<Status> getListSyncStatus() throws Exception {

        syncLock.lock();
        try {
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference colRef = db.collection("status");

            List<Status> localList = statusRepository.findAll();

            if (!isOnline()) {
                return localList;
            }

            Map<String, Status> processed = new HashMap<>();

            // 🔼 LOCAL → FIREBASE
            for (Status local : localList) {

                String fbId = local.getFirebaseId();

                if (fbId == null || fbId.isBlank()) {
                    fbId = colRef.document().getId();
                    local.setFirebaseId(fbId);
                    local.setLastSync(LocalDateTime.now());

                    colRef.document(fbId).set(Map.of(
                            "nom", local.getNom(),
                            "firebaseId", fbId,
                            "lastSync", local.getLastSync().toString()));

                    statusRepository.save(local);
                    processed.put(fbId, local);
                    continue;
                }

                DocumentSnapshot doc = colRef.document(fbId).get().get();

                if (!doc.exists()) {
                    local.setLastSync(LocalDateTime.now());
                    colRef.document(fbId).set(Map.of(
                            "nom", local.getNom(),
                            "firebaseId", fbId,
                            "lastSync", local.getLastSync().toString()));
                    statusRepository.save(local);
                }

                processed.put(fbId, local);
            }

            // 🔽 FIREBASE → LOCAL
            for (DocumentSnapshot doc : colRef.get().get().getDocuments()) {

                String fbId = doc.getString("firebaseId");
                if (fbId == null)
                    continue;

                Optional<Status> opt = statusRepository.findByFirebaseId(fbId);

                if (opt.isEmpty()) {
                    Status s = new Status();
                    s.setNom(doc.getString("nom"));
                    s.setFirebaseId(fbId);
                    s.setLastSync(parseFirebaseTimestamp(doc.getString("lastSync")));
                    statusRepository.save(s);
                }
            }

            return statusRepository.findAll();

        } finally {
            syncLock.unlock();
        }
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

}
