package com.example.carte.services;

import com.example.carte.entities.Entreprise;
import com.example.carte.entities.Profil;
import com.example.carte.entities.Status;
import com.example.carte.entities.Syncable;
import com.example.carte.entities.TypeSignalement;
import com.example.carte.repository.EntrepriseRepository;
import com.example.carte.repository.ProfilRepository;
import com.example.carte.repository.StatutRepository;
import com.example.carte.repository.TypeSignalementRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class SyncService {

    private final ProfilRepository profilRepo;
    private final EntrepriseRepository entrepriseRepo;
    private final StatutRepository statusRepo;
    private final TypeSignalementRepository typeSignalementRepo;

    public SyncService(
            ProfilRepository profilRepo,
            EntrepriseRepository entrepriseRepo,
            StatutRepository statusRepo,
            TypeSignalementRepository typeSignalementRepo) {
        this.profilRepo = profilRepo;
        this.entrepriseRepo = entrepriseRepo;
        this.statusRepo = statusRepo;
        this.typeSignalementRepo = typeSignalementRepo;
        // this.statutSignalementRepo = statutSignalementRepo;
    }

    @Transactional
    public void syncAllFirebaseEntities() {
        if (!isOnline())
            return;
        syncCollection(
                "type_signalement",
                name -> typeSignalementRepo.findByNom(name),
                typeSignalementRepo::save,
                TypeSignalement::new);

        syncCollection(
                "profils",
                profilRepo::findByNom, // Function<String, Optional<Profil>>
                profilRepo::save, // Function<Profil, Profil>
                Profil::new // Supplier<Profil> pour créer si absent
        );

        syncCollection(
                "entreprises",
                entrepriseRepo::findByNom,
                entrepriseRepo::save,
                Entreprise::new);

        syncCollection(
                "status",
                statusRepo::findByNom,
                statusRepo::save,
                Status::new);

    }

    private <T extends Syncable> void syncCollection(
            String collectionName,
            Function<String, Optional<T>> findLocalByName,
            Function<T, T> saveLocal,
            Supplier<T> constructor) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            List<QueryDocumentSnapshot> docs = db.collection(collectionName).get().get().getDocuments();
            for (QueryDocumentSnapshot doc : docs) {
                String name = doc.getString("nom");
                if (name == null)
                    continue;

                // Utilise Optional pour créer si absent
                T local = findLocalByName.apply(name).orElseGet(constructor);

                // Mettre à jour firebaseId et lastSync
                local.setFirebaseId(doc.getId());
                local.setLastSync(LocalDateTime.now());

                saveLocal.apply(local);
            }
        } catch (Exception e) {
            System.out.println("Erreur synchronisation Firestore pour " + collectionName + " : " + e.getMessage());
        }
    }

    private Syncable createNewInstanceForSync(String collectionName, String name) {
        switch (collectionName) {
            case "profil":
                Profil p = new Profil();
                p.setNom(name);
                return p;
            case "entreprise":
                Entreprise e = new Entreprise();
                e.setNom(name);
                return e;
            case "status":
                Status s = new Status();
                s.setNom(name);
                return s;
            case "type_signalement":
                TypeSignalement t = new TypeSignalement();
                t.setNom(name);
                return (Syncable) t;
            // case "statut_signalements":
            //     Status st = new Status();
            //     st.setNom(name);
            //     return st;
            default:
                throw new RuntimeException("Collection inconnue : " + collectionName);
        }
    }

    private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }
}
