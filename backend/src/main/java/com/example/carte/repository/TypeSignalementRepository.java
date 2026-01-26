package com.example.carte.repository;

import com.example.carte.entities.TypeSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeSignalementRepository extends JpaRepository<TypeSignalement, Integer> {

    Optional<TypeSignalement> findByNom(String nom);

    // Optional<TypeSignalement> findByNom(String nom);

    boolean existsByNom(String nom);
}
