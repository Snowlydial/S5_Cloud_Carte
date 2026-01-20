import { CompteRepository } from "@/repositories/CompteRepository";
import { Compte } from "@/models/Compte";
import { collection, getDocs } from "firebase/firestore";
import { db } from "@/firebase";
import { Recap } from "@/types/Recap";
import { SignalementRepository } from "@/repositories/SignalementRepository";
import { Signalement } from "@/models/Signalement";
import { SignalementService } from "./Signalement.service";
import { ProblemeService } from "./Probleme.service";
import { Probleme } from "@/models/Probleme";
import { ProfilRepository } from "@/repositories/ProfilRepository";
import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { ProblemeStatus } from "@/models/ProblemeStatus";
import { ProblemeStatusRepository } from "@/repositories/ProblemeStatusRepository";
import { ProblemeStatusService } from "./ProblemeStatus.service";

export class CompteService {

    static async getRecap(): Promise<Recap> {

        let nbrPoint = 0;
        let totalSurface = 0;
        let totalBudget = 0;



        let totalTermine = 0;
        let avancement = 0;


        let totalProbleme = 0;


        // const signalements: Signalement[] = await SignalementRepository.findAll();
        const problemes: Probleme[] = await ProblemeRepository.getAll();
        totalProbleme = problemes.length;
        nbrPoint = totalProbleme;
        for (const probleme of problemes) {
            // console.log("Problème ID:", probleme);
            if (probleme.idProbleme) {

                const status: ProblemeStatus | null = await ProblemeStatusService.getLastByIdProbleme(probleme.idProbleme);
                //         if (status && status.etat === "terminé") {
                //             totalTermine += 1;
            }
            totalSurface += probleme.surfaceM2;
            totalBudget += probleme.budget ?? 0;
        }

        // }
        // avancement  = totalProbleme > 0 ? Math.round((totalTermine / totalProbleme) * 100) : 0;



        // Logique fictive pour le récapitulatif
        const recap: Recap = {
            nbrPoint: nbrPoint, // Exemple de valeur
            totalSurface: totalSurface, // Exemple de valeur
            totalBudget: totalBudget, // Exemple de valeur
            avancement: avancement // Exemple de valeur en pourcentage
        };
        return recap;
    }

    static async create(data: Omit<Compte, "idCompte">): Promise<string> {
        try {
            const docRef = await CompteRepository.create(data);
            return docRef.id;
        } catch (error) {
            console.error("Erreur lors de la création du compte:", error);
            throw error;
        }
    }
    static async getById(id: string): Promise<Compte | null> {
        try {
            return await CompteRepository.findById(id);
        } catch (error) {
            console.error(`Erreur lors de la recherche du compte ${id}:`, error);
            throw error;
        }
    }
    static async findByEmail(email: string): Promise<Compte | null> {
        try {
            return await CompteRepository.findByEmail(email);
        } catch (error) {
            console.error(`Erreur lors de la recherche du compte ${email}:`, error);
            throw error;
        }
    }

    static async update(
        id: string,
        data: Partial<Omit<Compte, "id">>
    ): Promise<void> {
        try {
            await CompteRepository.update(id, data);
        } catch (error) {
            console.error(`Erreur lors de la mise à jour du compte ${id}:`, error);
            throw error;
        }
    }

    static async getAll(): Promise<Compte[]> {
        try {
            const snap = await getDocs(collection(db, CompteRepository.COLLECTION));
            return snap.docs.map(d => ({
                id: d.id,
                ...(d.data() as Omit<Compte, "id">)
            }));
        } catch (error) {
            console.error("Erreur lors de la récupération des comptes:", error);
            throw error;
        }
    }
}
