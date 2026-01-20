
import { Compte } from "@/models/Compte";
import { CompteRepository } from "@/repositories/CompteRepository";
import { Util } from "@/utils/util";
export class CompteSeeder {

    static async seed() {
        // const profils: Profil[] = [
        //     { nom: "administrateur" },
        //     { nom: "utilisateur" },
        // ];
        const comptes: Compte [] = [];
        await Util.clearCollection(CompteRepository.COLLECTION);

        for (const compte of comptes) {
            await CompteRepository.create(compte);
        }
    }

}