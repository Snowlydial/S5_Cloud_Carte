import { EntrepriseRepository } from "@/repositories/EntrepriseRepository";
import { Entreprise } from "@/models/Entreprise";

export class EntrepriseService {
  static async getAll(): Promise<Entreprise[]> {
    try {
        console.log ("Fetching all entreprises..." ,   EntrepriseRepository.getAll ());
      return await EntrepriseRepository.getAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des entreprises:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<Entreprise | null> {
    try {
      return await EntrepriseRepository.getById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération de l'entreprise ${id}:`, error);
      throw error;
    }
  }

  static async getByName(nom: string): Promise<Entreprise | null> {
    try {
      return await EntrepriseRepository.getByName(nom);
    } catch (error) {
      console.error(`Erreur lors de la récupération de l'entreprise ${nom}:`, error);
      throw error;
    }
  }

  static async create(data: Omit<Entreprise, "id">): Promise<string> {
    try {
      return await EntrepriseRepository.create(data);
    } catch (error) {
      console.error("Erreur lors de la création de l'entreprise:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<Entreprise, "id">>
  ): Promise<void> {
    try {
      await EntrepriseRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour de l'entreprise ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await EntrepriseRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression de l'entreprise ${id}:`, error);
      throw error;
    }
  }
}
