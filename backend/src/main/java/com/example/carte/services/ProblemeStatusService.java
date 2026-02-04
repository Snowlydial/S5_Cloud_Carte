package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.entities.Probleme;
import com.example.carte.entities.ProblemeStatus;
import com.example.carte.entities.Status;
import com.example.carte.repository.ProblemeRepository;
import com.example.carte.repository.ProblemeStatusRepository;
import com.example.carte.repository.StatutRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;
@Service
public class ProblemeStatusService {
    @Autowired
    private StatutRepository statutRepo;

    @Autowired
    private ProblemeStatusRepository problemeStatusRepository;

    @Autowired
        private  ProblemeRepository problemeRepo;

    private final ReentrantLock syncLock = new ReentrantLock();

    @Transactional
    public List<ProblemeStatus> getListSyncProblemeStatus() throws Exception {

        syncLock.lock();
        try {
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference colRef = db.collection("probleme_status");

            List<ProblemeStatus> localList = problemeStatusRepository.findAll();

            if (!isOnline()) {
                return localList;
            }

            Map<String, ProblemeStatus> processed = new HashMap<>();

            // 🔼 LOCAL → FIREBASE
            for (ProblemeStatus local : localList) {

                // ⚠️ sécurité FK
                if (local.getProbleme() == null || local.getStatus() == null) {
                    continue;
                }

                String fbId = local.getFirebaseId();

                if (fbId == null || fbId.isBlank()) {
                    fbId = colRef.document().getId();
                    local.setFirebaseId(fbId);
                    local.setLastSync(LocalDateTime.now());

                    colRef.document(fbId).set(buildProblemeStatusFirebaseMap(local));
                    problemeStatusRepository.save(local);
                    processed.put(fbId, local);
                    continue;
                }

                DocumentSnapshot doc = colRef.document(fbId).get().get();

                if (!doc.exists()) {
                    local.setLastSync(LocalDateTime.now());
                    colRef.document(fbId).set(buildProblemeStatusFirebaseMap(local));
                    problemeStatusRepository.save(local);
                }

                processed.put(fbId, local);
            }

            // 🔽 FIREBASE → LOCAL
            for (DocumentSnapshot doc : colRef.get().get().getDocuments()) {

                String fbId = doc.getString("firebaseId");
                if (fbId == null)
                    continue;

                if (processed.containsKey(fbId))
                    continue;

                Optional<ProblemeStatus> opt = problemeStatusRepository.findByFirebaseId(fbId);
                if (opt.isPresent())
                    continue;

                // 🔗 Résolution des dépendances
                String problemeFbId = doc.getString("idProbleme");
                String statusFbId = doc.getString("idStatus");

                Probleme probleme = problemeRepo.findByFirebaseId(problemeFbId)
                        .orElse(null);
                Status status = statutRepo.findByFirebaseId(statusFbId)
                        .orElse(null);

                if (probleme == null || status == null) {
                    continue; // ⚠️ dépendance manquante
                }

                ProblemeStatus ps = new ProblemeStatus();
                ps.setFirebaseId(fbId);
                ps.setEtat(doc.getString("etat"));
                ps.setDateStatus(parseFirebaseTimestamp(doc.getString("dateStatus")));
                ps.setLastSync(parseFirebaseTimestamp(doc.getString("lastSync")));
                ps.setProbleme(probleme);
                ps.setStatus(status);

                problemeStatusRepository.save(ps);
            }

            return problemeStatusRepository.findAll();

        } finally {
            syncLock.unlock();
        }
    }

    public boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
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

    private Map<String, Object> buildProblemeStatusFirebaseMap(ProblemeStatus ps) {

        Map<String, Object> map = new HashMap<>();
        map.put("firebaseId", ps.getFirebaseId());
        map.put("etat", ps.getEtat());
        map.put("dateStatus", ps.getDateStatus().toString());
        map.put("lastSync", ps.getLastSync().toString());
        map.put("idProbleme", ps.getProbleme().getFirebaseId());
        map.put("idStatus", ps.getStatus().getFirebaseId());
        return map;
    }

}
