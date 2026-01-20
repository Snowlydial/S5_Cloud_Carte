import { SignalementRepository } from "../repositories/SignalementRepository";
import { Signalement } from "../models/Signalement";

export class SignalementSeeder {
  static async seed() {
    const signalements: Omit<Signalement, "idSignalement">[] = [
      {
        dateSignalement: new Date(),
        longitude: 2.3522,
        latitude: 48.8566,
        idCompte: 1
      },
      {
        dateSignalement: new Date(Date.now() - 86400000),
        longitude: 2.3500,
        latitude: 48.8550,
        idCompte: 1
      },
      {
        dateSignalement: new Date(Date.now() - 172800000),
        longitude: 2.3550,
        latitude: 48.8600,
        idCompte: 2
      }
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
