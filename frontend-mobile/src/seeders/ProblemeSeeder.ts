import { Probleme } from "@/models/Probleme";
import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { EntrepriseService } from "@/services/Entreprise.service";
import { SignalementService } from "@/services/Signalement.service";
import { Util } from "@/utils/util";

export class ProblemeSeeder {

    static async seed() {
        await Util.clearCollection(ProblemeRepository.COLLECTION);

        const signalements = await SignalementService.getAll();
        const entreprise = (await EntrepriseService.getAll())[0];
        console.log("Entreprise pour les problèmes:", entreprise);

        for (const signalement of signalements) {
            for (let i = 0; i < 1; i++) {
                const probleme: Omit<Probleme, "idProbleme"> = {
                    dateProbleme: new Date(),
                    surfaceM2: Math.random() * 1000 + 50,
                    budget: Math.random() * 50000 + 5000,
                    idCompte: signalement.idCompte?.toString() || "",
                    idSignalement: signalement.idSignalement?.toString()     || "",
                    idEntreprise: entreprise.idEntreprise,
                };
                console.log ("Création du problème:", probleme);
                await ProblemeRepository.create(probleme);
            }
        }
    }

}
