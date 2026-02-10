package com.example.carte.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.carte.entities.Configuration;


public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {
}

