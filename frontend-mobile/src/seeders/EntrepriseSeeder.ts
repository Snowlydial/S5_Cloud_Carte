import { Entreprise } from "@/models/Entreprise";
import { EntrepriseRepository } from "@/repositories/EntrepriseRepository";
import { Util } from "@/utils/util";

export class EntrepriseSeeder {

    static async seed() {
        const entreprises: Entreprise[] = [
            { nom: "BTP Solutions" },
            { nom: "Construction Plus" },
            { nom: "Batiment Pro" },
            { nom: "Rénovation Expert" },
            { nom: "Travaux Généraux" },
        ];
        await Util.clearCollection(EntrepriseRepository.COLLECTION);

        for (const entreprise of entreprises) {
            await EntrepriseRepository.create(entreprise);
        }
    }

}
