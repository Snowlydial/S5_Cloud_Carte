import { Status } from "@/models/Status";
import { StatusRepository } from "@/repositories/StatusRepository";
import { Util } from "@/utils/util";

export class StatusSeeder {

    static async seed() {
        const statuses: Status[] = [
            { nom: "nouveau" },
            { nom: "en_cours" },
            { nom: "terminé" },
        ];
        await Util.clearCollection(StatusRepository.COLLECTION);

        for (const status of statuses) {
            await StatusRepository.create(status);
        }
    }

}
