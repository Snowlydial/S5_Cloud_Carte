package com.example.carte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Profil;

@Repository
public interface ProfilRepository extends JpaRepository<Profil, Integer> {
    Optional<Profil> findByNom(String name);
        Optional<Profil> findByFirebaseId(String firebaseId);

}
