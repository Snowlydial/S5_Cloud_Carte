package com.example.carte.repository;

import com.example.carte.entities.Signalement;
import com.example.carte.entities.SignalementImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SignalementImageRepository extends JpaRepository<SignalementImage, Integer> {
    List<SignalementImage> findBySignalement_IdSignalement(Integer idSignalement);

    Optional<SignalementImage> findByFirebaseId(String firebaseId);

}
