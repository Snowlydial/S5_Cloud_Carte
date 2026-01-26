package com.example.carte.repository;

import com.example.carte.entities.ProblemeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemeStatusRepository extends JpaRepository<ProblemeStatus, Integer> {
    // Ici tu peux ajouter des méthodes custom si besoin, par ex:
    // List<ProblemeStatus> findByProblemeIdOrderByDateStatusDesc(Integer idProbleme);
}
