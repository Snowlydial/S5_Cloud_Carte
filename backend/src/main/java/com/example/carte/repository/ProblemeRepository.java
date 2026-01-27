package com.example.carte.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Probleme;
import com.example.carte.entities.User;

@Repository
public interface ProblemeRepository extends JpaRepository<Probleme,Integer>{

    // User findUserByFirebaseUid(String uid);
// User findByCompteFirebaseId(String firebaseId);

//     User findUserByEmail(String email);

    List<Probleme> findByCompte(User user);

    Optional<Probleme> findByFirebaseId(String firebaseId); 

    @Query("SELECT DISTINCT p FROM Probleme p JOIN p.statusList ps WHERE ps.status.idStatus = :statusId")
    List<Probleme> findByStatut(@Param("statusId") Integer statusId);
    
}
