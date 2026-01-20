import { Profil } from "@/models/Profil";
import { ProfilRepository } from "@/repositories/ProfilRepository";
import { Util } from "@/utils/util";
import { collection, deleteDoc, doc, getDocs } from "firebase/firestore";

export class ProfilSeeder {

    static async seed() {
        const profils: Profil[] = [
            // { nom: "administrateur" },
            { nom: "utilisateur" },
        ];

        await Util.clearCollection(ProfilRepository.COLLECTION);

        for (const profil of profils) {
            await ProfilRepository.create(profil);
        }
    }

}