package com.example.carte.entities;

import java.time.LocalDateTime;

public interface Syncable {

    /** L’identifiant Firebase (documentId) */
    String getFirebaseId();
    void setFirebaseId(String firebaseId);

    /** Dernière synchronisation */
    LocalDateTime getLastSync();
    void setLastSync(LocalDateTime lastSync);

    /** Nom de la collection dans Firebase */
    String getCollectionName();
}
