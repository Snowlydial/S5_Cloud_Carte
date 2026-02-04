package com.example.carte.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.checkerframework.checker.units.qual.radians;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carte.dto.EntrepriseDTO;
import com.example.carte.entities.Entreprise;
import com.example.carte.repository.EntrepriseRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.transaction.Transactional;

@Service
public class EntrepriseService {

    @Autowired
    EntrepriseRepository entrepriseRepository;

    // @Transactional
    // public List<EntrepriseDTO> getListSyncEntreprises()
    //         throws InterruptedException, ExecutionException {

    //     Firestore db = FirestoreClient.getFirestore();
    //     CollectionReference colRef = db.collection("entreprise");

    //     List<Entreprise> localEntreprises = entrepriseRepository.findAll();

    //     if (isOnline()) {

    //         Map<String, Entreprise> localByFirebaseId = localEntreprises.stream()
    //                 .filter(e -> e.getFirebaseId() != null && !e.getFirebaseId().isBlank())
    //                 .collect(Collectors.toMap(Entreprise::getFirebaseId, e -> e));

    //         List<EntrepriseDTO> firebaseList = colRef.get().get().getDocuments()
    //                 .stream()
    //                 .map(this::mapFirestoreToDTOEntreprise)
    //                 .collect(Collectors.toList());

    //         for (EntrepriseDTO dto : firebaseList) {

    //             if (dto.getFirebaseId() == null || dto.getFirebaseId().isBlank())
    //                 continue;

    //             Entreprise local = entrepriseRepository
    //                     .findByFirebaseId(dto.getFirebaseId())
    //                     .orElse(null);

    //             if (local != null) {
    //                 // UPDATE local
    //                 local.setNom(dto.getNom());
    //                 entrepriseRepository.save(local);
    //                 localByFirebaseId.put(dto.getFirebaseId(), local);

    //             } else {
    //                 // CREATE local
    //                 Entreprise newLocal = mapDTOToEntityEntreprise(dto);
    //                 entrepriseRepository.save(newLocal);
    //                 localByFirebaseId.put(dto.getFirebaseId(), newLocal);
    //             }
    //         }

    //         // Sync local → Firebase
    //         for (Entreprise local : entrepriseRepository.findAll()) {

    //             String fbId = local.getFirebaseId();
    //             if (fbId == null || fbId.isBlank()) {
    //                 fbId = colRef.document().getId();
    //                 local.setFirebaseId(fbId);
    //                 entrepriseRepository.save(local);
    //             }

    //             Map<String, Object> map = new HashMap<>();
    //             map.put("idEntreprise", local.getIdEntreprise());
    //             map.put("nom", local.getNom());
    //             map.put("firebaseId", fbId);

    //             colRef.document(fbId).set(map);
    //         }

    //         return entrepriseRepository.findAll().stream()
    //                 .map(this::mapToDTOEntreprise)
    //                 .collect(Collectors.toList());
    //     }

    //     // Fallback offline
    //     return localEntreprises.stream()
    //             .map(this::mapToDTOEntreprise)
    //             .collect(Collectors.toList());
    // }
private final ReentrantLock syncLock = new ReentrantLock();
    
    @Transactional
    public List<EntrepriseDTO> getListSyncEntreprises()
            throws InterruptedException, ExecutionException {
        
        // 🔒 Acquérir le verrou - un seul thread à la fois
        syncLock.lock();
        try {
            System.out.println("🔍 ========== DEBUT SYNC ENTREPRISES ========== " + LocalDateTime.now());
            
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference colRef = db.collection("entreprise");

            List<Entreprise> localEntreprises = entrepriseRepository.findAll();

            if (!isOnline()) {
                return localEntreprises.stream()
                        .map(this::mapToDTOEntreprise)
                        .collect(Collectors.toList());
            }

            System.out.println("en ligne " + isOnline());

            // 🔑 CACHE pour tracker les firebaseId déjà traités
            Map<String, Entreprise> processedEntreprises = new HashMap<>();

            // 2️⃣ SYNC LOCAL → FIREBASE D'ABORD
            System.out.println("🔍 === PHASE LOCAL → FIREBASE ===");
            for (Entreprise local : localEntreprises) {
                String fbId = local.getFirebaseId();
                System.out.println("🔍 Traitement Entreprise ID=" + local.getIdEntreprise() + ", firebaseId=" + fbId);

                if (fbId == null || fbId.isBlank()) {
                    // ➕ CREATE Firebase
                    fbId = colRef.document().getId();
                    System.out.println("🔍   ➕ CREATE Firebase avec nouveau fbId: " + fbId);
                    local.setFirebaseId(fbId);
                    local.setLastSync(LocalDateTime.now());

                    Map<String, Object> map = buildEntrepriseFirebaseMap(local, fbId);
                    colRef.document(fbId).set(map);

                    entrepriseRepository.save(local);
                    processedEntreprises.put(fbId, local);
                    System.out.println("🔍   ✅ Créé et ajouté au cache");
                    continue;
                }

                // fbId existe → vérifier Firebase
                DocumentSnapshot firebaseDoc = colRef.document(fbId).get().get();

                if (!firebaseDoc.exists()) {
                    // 🔁 Firebase manquant → CREATE
                    System.out.println("🔍   🔁 Firebase manquant, CREATE");
                    local.setLastSync(LocalDateTime.now());

                    Map<String, Object> map = buildEntrepriseFirebaseMap(local, fbId);
                    colRef.document(fbId).set(map);

                    entrepriseRepository.save(local);
                    processedEntreprises.put(fbId, local);
                    System.out.println("🔍   ✅ Créé et ajouté au cache");
                    continue;
                }

                // Firebase existe → comparer lastSync
                LocalDateTime firebaseLastSync = parseFirebaseTimestamp(
                        firebaseDoc.getString("lastSync"));

                System.out.println("🔍   Firebase lastSync: " + firebaseLastSync + ", Local lastSync: " + local.getLastSync());

                if (local.getLastSync() != null &&
                        (firebaseLastSync == null ||
                                local.getLastSync().isAfter(firebaseLastSync))) {

                    // ⬆️ Local plus récent → UPDATE Firebase
                    System.out.println("🔍   ⬆️ Local plus récent, UPDATE Firebase");
                    local.setLastSync(LocalDateTime.now());

                    Map<String, Object> map = buildEntrepriseFirebaseMap(local, fbId);
                    colRef.document(fbId).set(map);

                    entrepriseRepository.save(local);
                } else {
                    System.out.println("🔍   ⏭️ Firebase à jour, skip");
                }

                processedEntreprises.put(fbId, local);
                System.out.println("🔍   ✅ Ajouté au cache");
            }

            System.out.println("🔍 Cache size après LOCAL→FIREBASE: " + processedEntreprises.size());
            System.out.println("🔍 Cache keys: " + processedEntreprises.keySet());

            // 3️⃣ SYNC FIREBASE → LOCAL (APRÈS avoir pushé le local)
            System.out.println("🔍 === PHASE FIREBASE → LOCAL ===");
            List<EntrepriseDTO> firebaseList = colRef.get().get().getDocuments()
                    .stream()
                    .map(this::mapFirestoreToDTOEntreprise)
                    .collect(Collectors.toList());

            System.out.println("🔍 Nombre d'entreprises Firebase: " + firebaseList.size());

            for (EntrepriseDTO firebaseDto : firebaseList) {
                String fbId = firebaseDto.getFirebaseId();
                System.out.println("🔍 Traitement Firebase fbId: " + fbId);
                
                if (fbId == null || fbId.isBlank()) {
                    System.out.println("🔍   ⚠️ fbId vide, skip");
                    continue;
                }

                // 🔑 VÉRIFIER D'ABORD LE CACHE
                if (processedEntreprises.containsKey(fbId)) {
                    System.out.println("🔍   ✅ Déjà dans le cache, vérification lastSync");
                    Entreprise existingLocal = processedEntreprises.get(fbId);

                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    firebaseDto.getLastSync().isAfter(existingLocal.getLastSync()))) {

                        System.out.println("🔍   ⬇️ Firebase plus récent, UPDATE local");
                        updateLocalEntrepriseFromFirebase(existingLocal, firebaseDto);
                        entrepriseRepository.save(existingLocal);
                    } else {
                        System.out.println("🔍   ⏭️ Local à jour, skip");
                    }
                    continue; // ⚠️ NE PAS créer de doublon
                }

                // Chercher dans la base de données (pour les entreprises d'autres utilisateurs)
                System.out.println("🔍   ⚠️ PAS dans le cache, recherche en base");
                Optional<Entreprise> localOpt = entrepriseRepository.findByFirebaseId(fbId);

                if (localOpt.isPresent()) {
                    System.out.println("🔍   ✅ Trouvé en base");
                    Entreprise existingLocal = localOpt.get();

                    if (firebaseDto.getLastSync() != null &&
                            (existingLocal.getLastSync() == null ||
                                    firebaseDto.getLastSync().isAfter(existingLocal.getLastSync()))) {

                        System.out.println("🔍   ⬇️ Firebase plus récent, UPDATE local");
                        updateLocalEntrepriseFromFirebase(existingLocal, firebaseDto);
                        entrepriseRepository.save(existingLocal);
                    }

                } else {
                    // ➕ Nouveau entreprise Firebase → CREATE local
                    System.out.println("🔍   ➕ NOUVEAU entreprise, CREATE local");
                    Entreprise newLocal = mapDTOToEntityEntreprise(firebaseDto);
                    newLocal.setFirebaseId(fbId);
                    newLocal.setLastSync(firebaseDto.getLastSync());

                    entrepriseRepository.save(newLocal);
                    System.out.println("🔍   ✅ Créé en local");
                }
            }

            System.out.println("🔍 ========== FIN SYNC ENTREPRISES ========== " + LocalDateTime.now());

            return entrepriseRepository.findAll().stream()
                    .map(this::mapToDTOEntreprise)
                    .collect(Collectors.toList());
                    
        } finally {
            // 🔒 Toujours libérer le verrou
            syncLock.unlock();
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void updateLocalEntrepriseFromFirebase(Entreprise local, EntrepriseDTO firebaseDto) {
        local.setNom(firebaseDto.getNom());
        local.setLastSync(firebaseDto.getLastSync());
    }

    private Map<String, Object> buildEntrepriseFirebaseMap(Entreprise local, String fbId) {
        Map<String, Object> map = new HashMap<>();
        map.put("idEntreprise", local.getIdEntreprise());
        map.put("nom", local.getNom());
        map.put("firebaseId", fbId);
        map.put("lastSync", local.getLastSync() != null ? local.getLastSync().toString() : null);
        return map;
    }

    private LocalDateTime parseFirebaseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            return null;
        }
    }
    private boolean isOnline() {
        try {
            return java.net.InetAddress.getByName("firebase.google.com").isReachable(1000);
        } catch (Exception e) {
            return false;
        }
    }

    private EntrepriseDTO mapFirestoreToDTOEntreprise(DocumentSnapshot doc) {
        EntrepriseDTO dto = new EntrepriseDTO();

        dto.setFirebaseId(doc.getId());
        dto.setIdEntreprise(doc.getLong("idEntreprise") != null
                ? doc.getLong("idEntreprise").intValue()
                : null);
        dto.setNom(doc.getString("nom"));

        return dto;
    }

    private Entreprise mapDTOToEntityEntreprise(EntrepriseDTO dto) {
        Entreprise entity = new Entreprise();
        // entity.setIdEntreprise(dto.getIdEntreprise());
        entity.setNom(dto.getNom());
        entity.setLastSync(LocalDateTime.now());
        entity.setFirebaseId(dto.getFirebaseId());

        return entity;
    }

    private EntrepriseDTO mapToDTOEntreprise(Entreprise entity) {
        EntrepriseDTO dto = new EntrepriseDTO();

        dto.setIdEntreprise(entity.getIdEntreprise());
        dto.setNom(entity.getNom());
        dto.setFirebaseId(entity.getFirebaseId());

        return dto;
    }

}
