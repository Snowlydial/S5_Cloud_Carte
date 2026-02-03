import { ref } from "vue";
import { Probleme } from "@/models/Probleme";
import { SignalementProbleme } from "@/models/SignalementProbleme";
import { Signalement } from "@/models/Signalement";
import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { SignalementService } from "@/services/Signalement.service";
import { SignalementRepository } from "@/repositories/SignalementRepository";
import { TypeSignalementRepository } from "@/repositories/TypeSignalementRepository";
import { EntrepriseRepository } from "@/repositories/EntrepriseRepository";
import { ProblemeStatusRepository } from "@/repositories/ProblemeStatusRepository";
import { StatusRepository } from "@/repositories/StatusRepository";
import { ProblemeStatus } from "@/models/ProblemeStatus";

// Exportation des références pour qu'elles soient accessibles
export const listeSignalementProbleme = ref<SignalementProbleme[]>([]);
export const loadingSP = ref(false);
export const errorSP = ref<string | null>(null);

/**
 * Charge les signalements et les fusionne avec les problèmes correspondants.
 * Met à jour la variable réactive 'listeSignalementProbleme'.
 */
export const useSignalementProbleme = () => {
  const fetchAllData = async () => {
    try {
      // 1. Chargement de toutes les collections en parallèle
      const [
        sigs, probs, types, ents, pStatus, statusLabels
      ] = await Promise.all([
        SignalementRepository.findAll(),
        ProblemeRepository.getAll(),
        TypeSignalementRepository.findAll(),
        EntrepriseRepository.getAll(),
        ProblemeStatusRepository.getAll(),
        StatusRepository.getAll()
      ]);

      // 2. Création de dictionnaires (Maps) pour un accès O(1)
      const typeMap = new Map(types.map(t => [t.idTypeSignalement.toString(), t]));
      const entMap = new Map(ents.map(e => [e.idEntreprise, e]));
      const statusLabelMap = new Map(statusLabels.map(s => [s.idStatus, s]));
      
      // Map pour les problèmes indexés par idSignalement
      const probMap = new Map(probs.map(p => [p.idSignalement, p]));

      // Map pour le status le plus récent par idProbleme
      // Comme pStatus est trié par date desc, le premier trouvé est le bon
      const latestStatusMap = new Map<string, ProblemeStatus>();
      pStatus.forEach(ps => {
        if (!latestStatusMap.has(ps.idProbleme)) {
          latestStatusMap.set(ps.idProbleme, ps);
        }
      });

      // 3. Assemblage du DTO
      listeSignalementProbleme.value = sigs.map(sig => {
        const prob = probMap.get(sig.idSignalement!);
        const type = typeMap.get(sig.idTypeSignalement.toString());
        const entreprise = prob?.idEntreprise ? entMap.get(prob.idEntreprise) : null;
        const pStat = prob?.idProbleme ? latestStatusMap.get(prob.idProbleme) : null;
        const sLabel = pStat ? statusLabelMap.get(pStat.idStatus) : null;

        return {
          ...sig,
          // Enrichissement Type
          typeNom: type?.nom || "Non spécifié",
          
          // Enrichissement Problème
          idProbleme: prob?.idProbleme,
          surfaceM2: prob?.surfaceM2,
          budget: prob?.budget,
          
          // Enrichissement Entreprise
          nomEntreprise: entreprise?.nom || "En attente d'affectation",
          
          // Enrichissement Status
          statusActuel: sLabel?.nom || "Nouveau",
          statusDate: pStat?.dateStatus
        };
      });

    } catch (error) {
      console.error("Erreur lors de la construction du DTO complet:", error);
    }
  };

  return { listeSignalementProbleme, fetchAllData };

  // const getSignalementsAvecProblemes = async (): Promise<SignalementProbleme[]> => {
  //   loadingSP.value = true;
  //   errorSP.value = null;

  //   try {
  //     // 1. Récupération parallèle des données
  //     const [allSignalements, allProblemes]: [Signalement[], Probleme[]] = await Promise.all([
  //       SignalementService.getAll(),
  //       ProblemeRepository.getAll()
  //     ]);

  //     // 2. Indexation des problèmes par idSignalement (Optimisation O(1))
  //     const problemesMap = new Map<string, Probleme>();
  //     allProblemes.forEach(p => {
  //       if (p.idSignalement) {
  //         problemesMap.set(p.idSignalement, p);
  //       }
  //     });

  //     // 3. Fusion des données
  //     const result: SignalementProbleme[] = allSignalements.map(sig => {
  //       const prob = sig.idSignalement ? problemesMap.get(sig.idSignalement) : null;

  //       return {
  //         ...sig,
  //         idProbleme: prob?.idProbleme,
  //         dateProbleme: prob?.dateProbleme || new Date(), // Valeur par défaut si nécessaire
  //         surfaceM2: prob?.surfaceM2 || 0,
  //         budget: prob?.budget || 0,
  //         idEntreprise: prob?.idEntreprise
  //       };
  //     });

  //     // 4. Mise à jour de la ref
  //     listeSignalementProbleme.value = result;
  //     return result;

  //   } catch (err) {
  //     const msg = "Erreur lors de la fusion Signalement/Probleme";
  //     console.error(msg, err);
  //     errorSP.value = msg;
  //     return [];
  //   } finally {
  //     loadingSP.value = false;
  //   }
  // };

  // return {
  //   // État
  //   listeSignalementProbleme,
  //   loadingSP,
  //   errorSP,
  //   // Actions
  //   getSignalementsAvecProblemes
  // };
};

export default useSignalementProbleme;