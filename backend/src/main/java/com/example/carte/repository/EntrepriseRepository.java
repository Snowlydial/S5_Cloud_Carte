package com.example.carte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Entreprise;

import ch.qos.logback.core.status.Status;
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {
     Optional<Entreprise> findByNom(String nom);
     Optional<Entreprise> findByFirebaseId(String firebaseId);
}