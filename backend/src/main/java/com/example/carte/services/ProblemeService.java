package com.example.carte.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.carte.dto.ProblemeDTO;
import com.example.carte.entities.Probleme;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.User;
import com.example.carte.repository.ProblemeRepository;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class ProblemeService {

    private final ProblemeRepository problemeRepo;
    private final SignalementRepository signalementRepo;
    private final UtilisateurRepository utilisateurRepository;

    public ProblemeService(ProblemeRepository problemeRepo, SignalementRepository signalementRepo,UtilisateurRepository utilisateurRepository) {
        this.problemeRepo = problemeRepo;
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Probleme> getAllProblemesRaw() {
        return problemeRepo.findAll();
    }

    public Probleme createProbleme(Probleme probleme) {

        // Vérifier le signalement
        Signalement signalement = signalementRepo.findById(probleme.getSignalement().getIdSignalement())
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
        probleme.setSignalement(signalement);

        // Always save locally
        Probleme saved = problemeRepo.save(probleme);

        // Si online, créer dans Firestore et mettre à jour firebaseId
        if (isOnline()) {
            Firestore db = FirestoreClient.getFirestore();
            try {
                DocumentReference docRef = db.collection("problemes").document();
                docRef.set(Map.of(
                        "dateProbleme", saved.getDateProbleme(),
                        "surfaceM2", saved.getSurfaceM2(),
                        "budget", saved.getBudget(),
                        "compteEmail", saved.getCompte().getEmail(),
                        "signalementId", saved.getSignalement().getIdSignalement())).get(); // .get() pour attendre la
                                                                                            // fin
                saved.setFirebaseId(docRef.getId());
                saved.setLastSync(LocalDateTime.now());
                saved = problemeRepo.save(saved); // update local avec firebaseId et lastSync
            } catch (Exception e) {
                System.out.println("Erreur Firestore : " + e.getMessage());
            }
        }

        return saved;
    }

    public List<ProblemeDTO> getProblemesHybrid(String firebaseUid, String email) {

        User user = null;

        if (isOnline() && firebaseUid != null && !firebaseUid.isEmpty()) {
            try {
                FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(firebaseUid);
                String uid = token.getUid();

                // ici tu devrais récupérer ton User local correspondant au firebaseUid
                user = (User) problemeRepo.findUserByFirebaseUid(uid);
                syncFirebaseProblemes(user);

            } catch (Exception e) {
                System.out.println("Firebase inaccessible, fallback local");
            }
        }

        if (user == null) {
            user = problemeRepo.findUserByEmail(email);
        }

        List<Probleme> problemes = problemeRepo.findByCompte(user);

        return problemes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ProblemeDTO> getAllProblemes() {
        List<Probleme> problemeList;

        if (isOnline()) {
            try {
                Firestore db = FirestoreClient.getFirestore();

                // Récupère tous les documents de la collection "problemes" depuis Firebase
                List<QueryDocumentSnapshot> docs = db.collection("problemes").get().get().getDocuments();

                for (QueryDocumentSnapshot doc : docs) {
                    Integer idFirebase = doc.contains("idProbleme") ? doc.getLong("idProbleme").intValue() : null;

                    // Chercher le problème localement
                    Probleme p = (idFirebase != null && problemeRepo.findById(idFirebase).isPresent())
                            ? problemeRepo.findById(idFirebase).get()
                            : new Probleme();

                    // Date du problème
                    if (doc.contains("dateProbleme") && doc.getTimestamp("dateProbleme") != null) {
                        p.setDateProbleme(
                                doc.getTimestamp("dateProbleme").toDate().toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime());
                    } else {
                        p.setDateProbleme(LocalDateTime.now());
                    }

                    // Surface et budget
                    p.setSurfaceM2(doc.contains("surfaceM2") ? doc.getDouble("surfaceM2") : 0.0);
                    p.setBudget(doc.contains("budget") ? doc.getDouble("budget") : 0.0);

                    // Compte lié
                    if (doc.contains("compteEmail")) {
                        User compte = utilisateurRepository.findByEmail(doc.getString("compteEmail"))
                                .orElseThrow(() -> new RuntimeException(
                                        "Utilisateur local introuvable pour " + doc.getString("compteEmail")));
                        p.setCompte(compte);
                    }

                    // Signalement lié
                    if (doc.contains("signalementId")) {
                        Signalement signalement = signalementRepo.findById(doc.getLong("signalementId").intValue())
                                .orElseThrow(() -> new RuntimeException(
                                        "Signalement local introuvable pour ID " + doc.getLong("signalementId")));
                        p.setSignalement(signalement);
                    }

                    // Mettre à jour les infos Firebase
                    p.setFirebaseId(doc.getId());
                    p.setLastSync(LocalDateTime.now());

                    // Sauvegarder localement
                    problemeRepo.save(p);
                }
            } catch (Exception e) {
                System.out.println("Firebase inaccessible, fallback local : " + e.getMessage());
            }
        }

        // Retourne tous les problèmes locaux
        problemeList = problemeRepo.findAll();

        // Mapper en DTO
        return problemeList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProblemeDTO mapToDTO(Probleme probleme) {
        ProblemeDTO dto = new ProblemeDTO();
        dto.setIdProbleme(probleme.getIdProbleme());
        dto.setDateProbleme(probleme.getDateProbleme());
        dto.setSurfaceM2(probleme.getSurfaceM2());
        dto.setBudget(probleme.getBudget());
        dto.setEntrepriseNom(probleme.getEntreprise() != null ? probleme.getEntreprise().getNom() : null);
        dto.setCompteEmail(probleme.getCompte() != null ? probleme.getCompte().getEmail() : null);
        dto.setSignalementId(probleme.getSignalement() != null ? probleme.getSignalement().getIdSignalement() : null);
        dto.setStatut(probleme.getStatusList() != null && !probleme.getStatusList().isEmpty()
                ? probleme.getStatusList().get(0).getEtat()
                : "OUVERT"); // premier statut ou "OUVERT" par défaut
        return dto;
    }

    public boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Probleme> findProblemeById(Integer id) {
        // Récupération purement locale
        return problemeRepo.findById(id);
    }

    private void syncFirebaseProblemes(User user) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // On récupère tous les documents de la collection "problemes" pour cet
            // utilisateur
            List<QueryDocumentSnapshot> docs = db.collection("problemes")
                    .whereEqualTo("compteEmail", user.getEmail())
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot doc : docs) {
                Integer idFirebase = doc.contains("idProbleme") ? doc.getLong("idProbleme").intValue() : null;
                Probleme p = problemeRepo.findById(idFirebase).orElse(new Probleme());

                if (doc.contains("dateProbleme")) {
                    Timestamp ts = doc.getTimestamp("dateProbleme"); // Firestore Timestamp
                    LocalDateTime date = ts.toDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    p.setDateProbleme(date);
                } else {
                    p.setDateProbleme(LocalDateTime.now());
                }
                p.setSurfaceM2(doc.contains("surfaceM2") ? doc.getDouble("surfaceM2") : 0.0);
                p.setBudget(doc.contains("budget") ? doc.getDouble("budget") : 0.0);
                p.setCompte(user);
                p.setSignalement(doc.contains("signalementId")
                        ? signalementRepo.findById(doc.getLong("signalementId").intValue()).orElse(null)
                        : null);
                p.setLastSync(LocalDateTime.now());

                problemeRepo.save(p);
            }

        } catch (Exception e) {
            System.out.println("Erreur synchronisation Firebase : " + e.getMessage());
        }
    }
}
