package com.example.carte.services;

import com.example.carte.entities.SignalementImage;
import com.example.carte.repository.SignalementImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class SignalementImageService {

    private final SignalementImageRepository repo;

    public SignalementImageService(SignalementImageRepository repo) {
        this.repo = repo;
    }

    // // Ajouter une image à un signalement
    // public SignalementImage saveImage(Integer idSignalement, MultipartFile file) throws IOException {
    //     byte[] bytes = file.getBytes();
    //     String base64 = Base64.getEncoder().encodeToString(bytes);

    //     SignalementImage image = new SignalementImage(base64, idSignalement);
    //     return repo.save(image);
    // }

    // // Récupérer toutes les images d’un signalement
    // public List<SignalementImage> getImagesBySignalement(Integer idSignalement) {
    //     return repo.findByIdSignalement(idSignalement);
    // }
}

