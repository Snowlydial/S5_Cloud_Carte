import { SignalementRepository } from "../repositories/SignalementRepository";
import { Signalement } from "../models/Signalement";
import { Util } from "@/utils/util";
import { CompteService } from "@/services/Compte.service";
import { TypeSignalementService } from "@/services/TypeSignalement.service";
import { CompteRepository } from "@/repositories/CompteRepository";
import { TypeSignalement } from "@/models/TypeSignalement";

export class SignalementSeeder {
  static async seed() {
    await Util.clearCollection(SignalementRepository.COLLECTION);

    const comptes = await CompteRepository.findAll();
    console.log("Comptes disponibles pour les signalements:", comptes);
    const typeSignalements : TypeSignalement[] = await TypeSignalementService.getAll();

    if (comptes.length === 0 || typeSignalements.length === 0) {
      console.log("Pas assez de données pour créer des signalements");
      return;
    }

    const firstCompte = comptes[0];
    const firstTypeSignalement = typeSignalements[0];

    console.log("Premier compte pour les signalements:", firstTypeSignalement); 
    console.log  ("les signalements vont être créés pour le compte:"+  typeSignalements);

    for (const element of typeSignalements) {
      
    }

    const signalements: Omit<Signalement, "idSignalement">[] = [
      {
        dateSignalement: new Date(),
        latitude: -18.8792,
        longitude: 47.5079,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
      {
        dateSignalement: new Date(),
        latitude: -18.8725,
        longitude: 47.5204,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
      {
        dateSignalement: new Date(),
        latitude: -18.9156,
        longitude: 47.5252,
        idCompte: firstCompte.idCompte || "",
        idTypeSignalement: firstTypeSignalement.idTypeSignalement || "",
      },
    ];



    for (const signalement of signalements) {
      try {
        await SignalementRepository.create(signalement);
        console.log("Signalement créé à: " +  signalement.idTypeSignalement);
      } catch (error) {
        console.error(`Erreur lors de la création du signalement:`, error);
      }
    }
  }
}
