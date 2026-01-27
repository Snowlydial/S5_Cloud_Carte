package com.example.carte.services;

import com.example.carte.entities.Syncable;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SyncService2 {

    /**
     * Synchronisation simple : local -> Firebase
     */
    public <T extends Syncable> T syncToFirebase(T entity) {
        if (!isOnline()) {
            return entity; // pas d'internet, on reste local
        }

        try {
            Firestore db = FirestoreClient.getFirestore();

            if (entity.getFirebaseId() == null || entity.getFirebaseId().isEmpty()) {
                // Créer un nouveau document Firebase
                var docRef = db.collection(entity.getCollectionName()).document();
                entity.setFirebaseId(docRef.getId());
                docRef.set(entity);
            } else {
                // Mettre à jour le document existant
                db.collection(entity.getCollectionName())
                  .document(entity.getFirebaseId())
                  .set(entity);
            }

            // Mettre à jour le local
            entity.setLastSync(LocalDateTime.now());
            return entity;

        } catch (Exception e) {
            System.out.println("Erreur sync Firebase : " + e.getMessage());
            return entity;
        }
    }

    /**
     * Détection simple de la connexion internet
     */
    private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }
}
