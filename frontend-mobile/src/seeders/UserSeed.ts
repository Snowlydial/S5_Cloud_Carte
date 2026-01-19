
import { auth } from "@/firebase";
import { Compte } from "@/models/Compte";
import { CompteRepository } from "@/repositories/CompteRepository";
import { Util } from "@/utils/util";
export class UserSeeder {

    static async seed() {
        const user = auth.currentUser;
        console.log("utilisateur:", user);
        await user?.delete();
    }

}