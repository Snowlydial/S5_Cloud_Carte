package com.example.carte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Signalement;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Integer> {

    Optional<Signalement> findByFirebaseId(String firebaseId);

}
