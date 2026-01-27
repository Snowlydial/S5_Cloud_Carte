package com.example.carte.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.EntrepriseDTO;
import com.example.carte.dto.ProblemeDTO;
import com.example.carte.dto.ProblemeStatusData;
import com.example.carte.dto.RecapDashboardDTO;
import com.example.carte.entities.Entreprise;
import com.example.carte.entities.Probleme;
import com.example.carte.entities.ProblemeStatus;
import com.example.carte.entities.Signalement;
import com.example.carte.entities.Status;
import com.example.carte.entities.User;
import com.example.carte.repository.EntrepriseRepository;
import com.example.carte.repository.ProblemeRepository;
import com.example.carte.repository.ProblemeStatusRepository;
import com.example.carte.repository.SignalementRepository;
import com.example.carte.repository.StatutRepository;
import com.example.carte.repository.UtilisateurRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

@Service
public class ProblemeService {

    private final ProblemeRepository problemeRepo;
    private final SignalementRepository signalementRepo;
    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepo;
    private final StatutRepository statutRepo;
    @Autowired
    private ProblemeStatusRepository problemeStatusRepository;
    @Autowired
    private EntrepriseService entrepriseService;
    @Autowired
    private UserService userService;

    public ProblemeService(ProblemeRepository problemeRepo, SignalementRepository signalementRepo,
            UtilisateurRepository utilisateurRepository,
            EntrepriseRepository entrepriseRepo,
            StatutRepository statutRepo) {
        this.problemeRepo = problemeRepo;
        this.signalementRepo = signalementRepo;
        this.utilisateurRepository = utilisateurRepository;
        this.entrepriseRepo = entrepriseRepo;
        this.statutRepo = statutRepo;
    }

    public List<Probleme> getAllProblemesRaw() {
        return problemeRepo.findAll();
    }

    public void syncFireBaseProbleme() throws Exception {
        List<ProblemeDTO> dtos = getListSyncProblemes();
    }

    @Transactional
    public List<ProblemeDTO> getListSyncProblemes() throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        CollectionReference colRef = db.collection("problemes");

        List<Probleme> localProblemes = problemeRepo.findAll();

        if (isOnline()) {

            Map<String, Probleme> localByFirebaseId = localProblemes.stream()
                    .filter(p -> p.getFirebaseId() != null && !p.getFirebaseId().isBlank())
                    .collect(Collectors.toMap(Probleme::getFirebaseId, p -> p));

            List<ProblemeDTO> firebaseList = colRef.get().get().getDocuments()
                    .stream()
                    .map(this::mapFirestoreToProblemeDTO)
                    .collect(Collectors.toList());

            for (ProblemeDTO dto : firebaseList) {
                String fbId = dto.getFirebaseId();
                if (fbId == null || fbId.isBlank())
                    continue;

                Probleme local = problemeRepo.findByFirebaseId(fbId).orElse(null);

                if (local != null) {
                    // UPDATE
                    local.setSurfaceM2(dto.getSurfaceM2());
                    local.setBudget(dto.getBudget());
                    local.setDateProbleme(dto.getDateProbleme());

                    if (dto.getCompteEmail() != null) {
                        User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                                .orElseThrow(() -> new RuntimeException("Compte introuvable"));
                        local.setCompte(compte);
                    }

                    if (dto.getSignalementId() != null) {
                        Signalement s = signalementRepo.findById(dto.getSignalementId())
                                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
                        local.setSignalement(s);
                    }

                    local.setLastSync(LocalDateTime.now());
                    problemeRepo.save(local);
                    localByFirebaseId.put(fbId, local);

                } else {
                    // INSERT
                    Probleme p = mapDTOToProbleme(dto);
                    p.setFirebaseId(fbId);
                    p.setLastSync(LocalDateTime.now());
                    problemeRepo.save(p);
                    localByFirebaseId.put(fbId, p);
                }
            }
            // syncena ilay local
            for (Probleme local : localProblemes) {

                String fbId = local.getFirebaseId();
                if (fbId == null || fbId.isBlank()) {
                    fbId = db.collection("problemes").document().getId();
                    local.setFirebaseId(fbId);
                    problemeRepo.save(local);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("idProbleme", local.getIdProbleme());
                data.put("dateProbleme", local.getDateProbleme().toString());
                data.put("surfaceM2", local.getSurfaceM2());
                data.put("budget", local.getBudget());
                data.put("firebaseId", fbId);
                data.put("compteEmail", local.getCompte() != null ? local.getCompte().getEmail() : null);
                // data.put("idEntreprise", local.get)
                // verification de l'entreprise
                if (local.getEntreprise().getFirebaseId() == null) {
                    // syncer
                    List<EntrepriseDTO> dto = entrepriseService.getListSyncEntreprises();
                }
                if (local.getCompte().getFirebaseUid() == null) {
                    // syncer
                    User u = userService.getById(local.getCompte().getId());
                    userService.syncCompteLocalToFirebase(u);
                }
                data.put("entrepriseNom", local.getEntreprise().getFirebaseId());
                data.put("idCompte", local.getCompte().getFirebaseUid());
                data.put("signalementId",
                        local.getSignalement() != null ? local.getSignalement().getIdSignalement() : null);
                data.put("idSignalement", local.getSignalement().getFirebaseId());
                db.collection("problemes").document(fbId).set(data);
            }
        }

        return problemeRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProblemeDTO mapFirestoreToProblemeDTO(QueryDocumentSnapshot doc) {

        ProblemeDTO dto = new ProblemeDTO();

        dto.setIdProbleme(doc.contains("idProbleme") ? doc.getLong("idProbleme").intValue() : null);

        if (doc.contains("dateProbleme")) {
            Object rawDate = doc.get("dateProbleme");

            if (rawDate instanceof com.google.cloud.Timestamp ts) {
                dto.setDateProbleme(
                        ts.toDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime());
            } else if (rawDate instanceof String dateStr) {
                dto.setDateProbleme(
                        LocalDateTime.parse(dateStr));
            }
        }

        dto.setSurfaceM2(doc.contains("surfaceM2") ? doc.getDouble("surfaceM2") : 0.0);
        dto.setBudget(doc.contains("budget") ? doc.getDouble("budget") : 0.0);
        dto.setCompteEmail(doc.getString("compteEmail"));
        dto.setIdSignalement(doc.getString("idSignalement"));
        dto.setSignalementId(doc.contains("signalementId") ? doc.getLong("signalementId").intValue() : null);
        dto.setFirebaseId(doc.getId());

        return dto;
    }

    private Probleme mapDTOToProbleme(ProblemeDTO dto) {

        Probleme p = new Probleme();
        p.setDateProbleme(dto.getDateProbleme() != null ? dto.getDateProbleme() : LocalDateTime.now());
        p.setSurfaceM2(dto.getSurfaceM2());
        p.setBudget(dto.getBudget());

        if (dto.getCompteEmail() != null) {
            User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                    .orElseThrow(() -> new RuntimeException("Compte introuvable"));
            p.setCompte(compte);

        }

        if (dto.getSignalementId() != null) {
            Signalement s = signalementRepo.findById(dto.getSignalementId())
                    .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
            p.setSignalement(s);
        }
        if (dto.getEntrepriseNom() != null) {
            Entreprise e = entrepriseRepo.findById(dto.getEntrepriseNom())
                    .orElseThrow(() -> new RuntimeException("entreprise introuvable"));
            p.setEntreprise(e);
        }

        return p;
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
                user = (User) utilisateurRepository.findByFirebaseUid(uid).get();
                syncFirebaseProblemes(user);

            } catch (Exception e) {
                System.out.println("Firebase inaccessible, fallback local");
            }
        }

        if (user == null) {
            user = utilisateurRepository.findByEmail(email).get();
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

    public ProblemeDTO mapToDTO(Probleme probleme) {
        ProblemeDTO dto = new ProblemeDTO();
        dto.setIdProbleme(probleme.getIdProbleme());
        dto.setDateProbleme(probleme.getDateProbleme());
        dto.setSurfaceM2(probleme.getSurfaceM2());
        dto.setBudget(probleme.getBudget());
        dto.setEntrepriseNom(probleme.getEntreprise() != null ? probleme.getEntreprise().getIdEntreprise() : null);
        dto.setCompteEmail(probleme.getCompte() != null ? probleme.getCompte().getEmail() : null);
        dto.setSignalementId(probleme.getSignalement() != null ? probleme.getSignalement().getIdSignalement() : null);
        dto.setStatut(probleme.getStatusList().getLast().getStatus().getIdStatus());
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

    @Transactional
    public ProblemeDTO createProbleme(ProblemeDTO dto) {

        Signalement signalement = signalementRepo.findById(dto.getSignalementId())
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        User compte = utilisateurRepository.findByEmail(dto.getCompteEmail()).get();

        Entreprise entreprise = null;
        if (dto.getEntrepriseNom() != null) {
            entreprise = entrepriseRepo.findById(Integer.valueOf(dto.getEntrepriseNom()))
                    .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        }

        // 🔹 Vérifier que le statut existe, sinon utiliser EN_ATTENTE par défaut
        Integer statutValue = dto.getStatut() != null ? dto.getStatut() : 1;
        Status statut = statutRepo.findById(statutValue).get();

        Probleme probleme = new Probleme();
        probleme.setDateProbleme(LocalDateTime.now());
        probleme.setSurfaceM2(dto.getSurfaceM2());
        probleme.setBudget(dto.getBudget());
        probleme.setEntreprise(entreprise != null ? entreprise : null);
        probleme.setCompte(compte);
        probleme.setSignalement(signalement);

        ProblemeStatus problemeStatus = new ProblemeStatus();
        problemeStatus.setEtat(statut.getNom());
        problemeStatus.setStatus(statut);
        problemeStatus.setDateStatus(LocalDateTime.now());
        problemeStatus.setProbleme(probleme);
        if (probleme.getStatusList() == null) {
            probleme.setStatusList(new java.util.LinkedList<>());
        }
        probleme.getStatusList().add(problemeStatus);
        Probleme saved = problemeRepo.save(probleme);

        dto.setIdProbleme(saved.getIdProbleme());
        dto.setDateProbleme(saved.getDateProbleme());
        dto.setStatut(saved.getStatusList().getLast().getStatus().getIdStatus());
        return dto;
    }

    @Transactional
    public RecapDashboardDTO getRecapActuel2() {
        RecapDashboardDTO recapDashboardDTO = new RecapDashboardDTO();
        // get nb de points total
        // get total surface
        // % d'avancement
        // total budget
        List<Probleme> problemes = problemeRepo.findAll();
        double surface_total = 0;
        int nb_points = problemes.size();
        double avancement = 0;
        double total_budget = 0;
        for (Probleme probleme : problemes) {
            surface_total += probleme.getSurfaceM2();
            avancement += probleme.getAvancement();
            total_budget += probleme.getBudget();
        }
        avancement = avancement / nb_points;
        recapDashboardDTO.setAvancementPercent(avancement);
        recapDashboardDTO.setNbPoints(nb_points);
        recapDashboardDTO.setTotalBudget(total_budget);
        recapDashboardDTO.setTotalSurface(total_budget);
        recapDashboardDTO.setTotalSurface(surface_total);
        return recapDashboardDTO;
    }

    public RecapDashboardDTO getRecapActuel() {

        RecapDashboardDTO recap = new RecapDashboardDTO();

        List<Probleme> problemes = problemeRepo.findAll();

        int nbPoints = problemes.size();
        double totalSurface = 0;
        double totalBudget = 0;
        double avancementTotal = 0;

        for (Probleme probleme : problemes) {

            totalSurface += probleme.getSurfaceM2();
            totalBudget += probleme.getBudget();

            // 🔹 Récupérer le dernier status du problème
            ProblemeStatus lastStatus = problemeStatusRepository.findTopByProblemeOrderByDateStatusDesc(probleme);

            if (lastStatus != null) {
                switch (lastStatus.getStatus().getNom().toLowerCase()) {
                    case "termine" -> avancementTotal += 100;
                    case "en_cours" -> avancementTotal += 50;
                    case "nouveau" -> avancementTotal += 0;
                }
            }
        }

        double avancementPercent = nbPoints == 0 ? 0 : avancementTotal / nbPoints;

        recap.setNbPoints(nbPoints);
        recap.setTotalSurface(totalSurface);
        recap.setTotalBudget(totalBudget);
        recap.setAvancementPercent(avancementPercent);

        return recap;
    }

    @Transactional
    public ProblemeDTO updateStatus(Integer idProbleme, ProblemeStatusData statusData) {

        System.out.println(statusData.toJsonString());
        Probleme probleme = problemeRepo.findById(idProbleme)
                .orElseThrow(() -> new RuntimeException("Problème introuvable"));

        Status status = statutRepo.findById(statusData.getIdStatus())
                .orElseThrow(() -> new RuntimeException("Status introuvable : " + statusData.getIdStatus()));

        ProblemeStatus problemeStatus = new ProblemeStatus();
        problemeStatus.setProbleme(probleme);
        problemeStatus.setStatus(status);
        problemeStatus.setEtat(statusData.getEtat());
        // Si dateStatus fourni, sinon maintenant
        problemeStatus.setDateStatus(
                statusData.getDateStatus() != null ? statusData.getDateStatus() : LocalDateTime.now());

        problemeStatusRepository.save(problemeStatus);

        // probleme.(status);
        problemeRepo.save(probleme);

        return mapToDTO(probleme);
    }

    @Transactional
    public ProblemeDTO updateProbleme(ProblemeDTO dto, Integer idProbleme) {

        Probleme probleme = problemeRepo.findById(idProbleme)
                .orElseThrow(() -> new RuntimeException("Problème introuvable"));

        // Mettre à jour les champs simples
        if (dto.getSurfaceM2() != null) {
            probleme.setSurfaceM2(dto.getSurfaceM2());
        }

        if (dto.getBudget() != null) {
            probleme.setBudget(dto.getBudget());
        }

        if (dto.getDateProbleme() != null) {
            probleme.setDateProbleme(dto.getDateProbleme());
        }

        // Mettre à jour l'entreprise si fourni
        if (dto.getEntrepriseNom() != null) {
            Entreprise entreprise = entrepriseRepo.findById(Integer.valueOf(dto.getEntrepriseNom()))
                    .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
            probleme.setEntreprise(entreprise);
        }

        // Mettre à jour le compte si fourni
        if (dto.getCompteEmail() != null) {
            User compte = utilisateurRepository.findByEmail(dto.getCompteEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            probleme.setCompte(compte);
        }

        // Mettre à jour le signalement si fourni
        if (dto.getSignalementId() != null) {
            Signalement signalement = signalementRepo.findById(dto.getSignalementId())
                    .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
            probleme.setSignalement(signalement);
        }

        // Mettre à jour le statut si fourni
        if (dto.getStatut() != null) {
            Status status = statutRepo.findById(dto.getStatut())
                    .orElseThrow(() -> new RuntimeException("Status introuvable"));
            ProblemeStatus problemeStatus = new ProblemeStatus();
            problemeStatus.setProbleme(probleme);
            problemeStatus.setStatus(status);
            problemeStatus.setEtat(status.getNom());
            problemeStatus.setDateStatus(LocalDateTime.now());
            problemeStatusRepository.save(problemeStatus);
        }

        // Sauvegarder le problème
        Probleme updated = problemeRepo.save(probleme);

        // Retourner le DTO mis à jour
        return mapToDTO(updated);
    }

    public List<ProblemeDTO> getProblemesByStatus(Integer statusId) {
        List<Probleme> problemes = problemeRepo.findByStatut(statusId);
        return problemes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

}
