import { SignalementRepository } from "../repositories/SignalementRepository";
import { Signalement } from "../models/Signalement";
import { Util } from "@/utils/util";

export class SignalementSeeder {
  static async seed() {
    const signalements: Omit<Signalement, "idSignalement">[] = [];

    Util.clearCollection(SignalementRepository.COLLECTION);
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
