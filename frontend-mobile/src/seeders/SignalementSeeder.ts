import { SignalementRepository } from "../repositories/SignalementRepository";
import { Signalement } from "../models/Signalement";
import { Util } from "@/utils/util";
import { CompteService } from "@/services/Compte.service";
import { TypeSignalementService } from "@/services/TypeSignalement.service";
import { CompteRepository } from "@/repositories/CompteRepository";

export class SignalementSeeder {
  static async seed() {
    await Util.clearCollection(SignalementRepository.COLLECTION);

    const comptes = await CompteRepository.findAll();
    console.log ("Comptes disponibles pour les signalements:", comptes);
    const typeSignalements = await TypeSignalementService.getAll();

    if (comptes.length === 0 || typeSignalements.length === 0) {
      console.log("Pas assez de données pour créer des signalements");
      return;
    }

    const firstCompte = comptes[0];
    const firstTypeSignalement = typeSignalements[0];

    const signalements: Omit<Signalement, "idSignalement">[] = [
      {
        dateSignalement: new Date(),
        latitude: 48.8566,
        longitude: 2.3522,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
      {
        dateSignalement: new Date(),
        latitude: 48.8606,
        longitude: 2.2936,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
      {
        dateSignalement: new Date(),
        latitude: 48.8530,
        longitude: 2.3499,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
    ];

    for (const signalement of signalements) {
      try {
        await SignalementRepository.create(signalement);
        console.log(`Signalement créé à [${signalement.latitude}, ${signalement.longitude}].`);
      } catch (error) {
        console.error(`Erreur lors de la création du signalement:`, error);
      }
    }
  }
}
