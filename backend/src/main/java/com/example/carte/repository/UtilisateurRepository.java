package com.example.carte.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.User;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByFirebaseUid(String firebaseUid);
}

