package com.example.carte.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.carte.dto.StatusDTO;
import com.example.carte.entities.Status;
import com.example.carte.repository.StatutRepository;

@Service
public class StatusService {

    private final StatutRepository statusRepository;

    public StatusService(StatutRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<StatusDTO> getAllStatus() {
        return statusRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private StatusDTO mapToDTO(Status status) {
        StatusDTO dto = new StatusDTO();
        dto.setIdStatus(status.getIdStatus());
        dto.setNom(status.getNom());
        return dto;
    }
}

