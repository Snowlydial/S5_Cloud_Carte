import { Util } from "@/utils/util";
import { TypeSignalementRepository } from "../repositories/TypeSignalementRepository";

export class TypeSignalementSeeder {
    static async seed() {
        const typeSignalements = [
            { nom: "Nid de poule" , idimage: '1'},
            { nom: "Route endommagée" , idimage: '2'},
            { nom: "Éclairage défaillant" , idimage: '3'},
            { nom: "Signalisation insuffisante" , idimage: '4'},
            { nom: "Débris sur la chaussée", idimage: '5'},
            { nom: "Accident de la circulation" , idimage: '6'},
            { nom: "Conducteur en danger" , idimage: '7'}
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
