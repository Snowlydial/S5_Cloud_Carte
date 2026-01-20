import { Util } from "@/utils/util";
import { TypeSignalementRepository } from "../repositories/TypeSignalementRepository";

export class TypeSignalementSeeder {
    static async seed() {
        const typeSignalements = [
            { nom: "Nid de poule" },
            { nom: "Route endommagée" },
            { nom: "Éclairage défaillant" },
            { nom: "Signalisation insuffisante" },
            { nom: "Débris sur la chaussée" },
            { nom: "Accident de la circulation" },
            { nom: "Conducteur en danger" }
        ];

        Util.clearCollection (TypeSignalementRepository.COLLECTION);
        for (const typeSignalement of typeSignalements) {
            try {
                const existing = await TypeSignalementRepository.findByNom(typeSignalement.nom);

                // if (!existing) {
                await TypeSignalementRepository.create(typeSignalement);
                console.log(`TypeSignalement '${typeSignalement.nom}' créé.`);
                // } else {
                //   console.log(`TypeSignalement '${typeSignalement.nom}' existe déjà.`);
                // }
            } catch (error) {
                console.error(`Erreur lors de la création du TypeSignalement '${typeSignalement.nom}':`, error);
            }
        }
    }
}
