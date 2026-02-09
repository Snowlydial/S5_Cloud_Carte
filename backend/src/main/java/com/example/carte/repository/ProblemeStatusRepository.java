package com.example.carte.repository;

import com.example.carte.entities.Probleme;
import com.example.carte.entities.ProblemeStatus;
import com.example.carte.entities.Status;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemeStatusRepository extends JpaRepository<ProblemeStatus, Integer> {
    // Ici tu peux ajouter des méthodes custom si besoin, par ex:
    Optional<ProblemeStatus> findByFirebaseId(String firebaseId);

    ProblemeStatus findTopByProblemeOrderByDateStatusDesc(Probleme probleme);

    List<ProblemeStatus> findByProbleme(Probleme probleme, Pageable pageable);

    @Query(value = """
            SELECT (date2::date - date1::date) as datediff from (SELECT date_status date1,id_probleme from probleme_status ps
            join status s on s.id_status=ps.id_status
            where id_probleme=? and s.nom='nouveau') as stat_nouveau
            join
            (SELECT date_status date2,id_probleme from probleme_status ps
            join status s on s.id_status=ps.id_status
            where id_probleme=? and s.nom='termine') as stat_termine
            on stat_nouveau.id_probleme=stat_termine.id_probleme
                        """, nativeQuery = true)
    public Integer findDiffDate(
            @Param("idProbleme") Integer id_probleme);
}
