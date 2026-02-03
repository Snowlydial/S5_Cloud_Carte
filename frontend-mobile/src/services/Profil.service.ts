import { Profil } from "@/models/Profil";
import { ProfilRepository } from "@/repositories/ProfilRepository";

export  class ProfilService {


    static async getAll (): Promise<Profil []>{
        const profils: Profil [] = await ProfilRepository.getAll();
        return profils;
    }

    static async getByName  (name: string): Promise<Profil | null>{
        const profil: Profil | null = await ProfilRepository.getByName(name);
        return profil;
    }

    static async getById (id: string): Promise<Profil | null>{
        const profil: Profil | null = await ProfilRepository.getById(id);
        return profil;
    }
    
}