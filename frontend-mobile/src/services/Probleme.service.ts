import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { Probleme } from "@/models/Probleme";

export class ProblemeService {
  static async getAll(): Promise<Probleme[]> {
    try {
      return await ProblemeRepository.getAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des problèmes:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<Probleme | null> {
    try {
      return await ProblemeRepository.getById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération du problème ${id}:`, error);
      throw error;
    }
  }

  static async getByIdCompte(idCompte: string): Promise<Probleme[]> {
    try {
      return await ProblemeRepository.getByIdCompte(idCompte);
    } catch (error) {
      console.error(`Erreur lors de la récupération des problèmes du compte ${idCompte}:`, error);
      throw error;
    }
  }

  static async getByIdSignalement(idSignalement: string): Promise<Probleme[]> {
    try {
      return await ProblemeRepository.getByIdSignalement(idSignalement);
    } catch (error) {
      console.error(`Erreur lors de la récupération des problèmes du signalement ${idSignalement}:`, error);
      throw error;
    }
  }

  static async getByIdEntreprise(idEntreprise: string): Promise<Probleme[]> {
    try {
      return await ProblemeRepository.getByIdEntreprise(idEntreprise);
    } catch (error) {
      console.error(`Erreur lors de la récupération des problèmes de l'entreprise ${idEntreprise}:`, error);
      throw error;
    }
  }

  static async create(data: Omit<Probleme, "id">): Promise<string> {
    try {
      return await ProblemeRepository.create(data);
    } catch (error) {
      console.error("Erreur lors de la création du problème:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<Probleme, "id">>
  ): Promise<void> {
    try {
      await ProblemeRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour du problème ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await ProblemeRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression du problème ${id}:`, error);
      throw error;
    }
  }
}
