package com.example.carte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Entreprise;

import ch.qos.logback.core.status.Status;
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {}