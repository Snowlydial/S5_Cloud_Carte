package com.example.carte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Probleme;
import com.example.carte.entities.User;

@Repository
public interface ProblemeRepository extends JpaRepository<Probleme,Integer>{

    // User findUserByFirebaseUid(String uid);
// User findByCompteFirebaseId(String firebaseId);

//     User findUserByEmail(String email);

    List<Probleme> findByCompte(User user);
    
}
