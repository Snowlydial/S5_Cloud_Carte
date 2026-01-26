import { Probleme } from "@/models/Probleme";
import { ProblemeStatus } from "@/models/ProblemeStatus";
import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { ProblemeStatusRepository } from "@/repositories/ProblemeStatusRepository";
import { StatusRepository } from "@/repositories/StatusRepository";
import { EntrepriseService } from "@/services/Entreprise.service";
import { ProblemeService } from "@/services/Probleme.service";
import { SignalementService } from "@/services/Signalement.service";
import { StatusService } from "@/services/Status.service";
import { Util } from "@/utils/util";

export class ProblemeStatusSeeder {

    static async seed() {
        await Util.clearCollection(ProblemeStatusRepository.COLLECTION);

        const problems = await ProblemeService.getAll();

        const status  = (await StatusService.getAll())[0];


        // console.log("Entreprise pour les problèmes:", entreprise);
        for (const probleme of problems) {
            if (status == null) {
                console.log("Aucun status disponible pour les problèmes");
                return;
            }
            const problemeStatus: Omit<ProblemeStatus, "idProblemeStatus"> = {
                idProbleme: probleme.idProbleme?.toString() || "",
                etat: "Nouveau",
                dateStatus: new Date(),
                idStatus: status.idStatus || "",
            };
            console.log ("Création du problème status:", problemeStatus);
            await ProblemeStatusRepository.create(problemeStatus);
        }

    }

}
