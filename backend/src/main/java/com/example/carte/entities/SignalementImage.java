package com.example.carte.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "signalement_image")
public class SignalementImage implements Syncable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSignalementImage;

    // @Lob
    @Column(columnDefinition = "TEXT")
    private String lien; // Base64


    // Si tu veux une relation bidirectionnelle avec Signalement
    @ManyToOne
    @JoinColumn(name = "id_signalement")
    private Signalement signalement;

    public SignalementImage() {
    }

    // public SignalementImage(String lien, Integer idSignalement) {
    //     this.lien = lien;
    //     // this.idSignalement = idSignalement;
    // }

    @Override
    public String getCollectionName() {
        return "signalement_images";
    }

    @Column(name = "firebase_id")
    private String firebaseId;

    @Column(name = "last_sync")
    private LocalDateTime lastSync;

    // @Override
    // public String getFirebaseId() {
    // return this.firebaseId;
    // }

    // @Override
    // public void setFirebaseId(String firebaseId) {
    // this.firebaseId=
    // }

    // @Override
    // public LocalDateTime getLastSync() {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getLastSync'");
    // }

    // @Override
    // public void setLastSync(LocalDateTime lastSync) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'setLastSync'");
    // }
}
