package com.example.carte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carte.entities.Status;

@Repository
public interface StatutRepository extends JpaRepository<Status, Integer> {
}
