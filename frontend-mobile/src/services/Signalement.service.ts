import { SignalementRepository } from "@/repositories/SignalementRepository";
import { Signalement } from "@/models/Signalement";

export class SignalementService {
  static async getAll(): Promise<Signalement[]> {
    try {
      return await SignalementRepository.findAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des signalements:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<Signalement | null> {
    try {
      return await SignalementRepository.findById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération du signalement ${id}:`, error);
      throw error;
    }
  }

  static async getByIdCompte(idCompte: string): Promise<Signalement[]> {
    try {
      return await SignalementRepository.findByIdCompte(idCompte);
    } catch (error) {
      console.error(`Erreur lors de la récupération des signalements du compte ${idCompte}:`, error);
      throw error;
    }
  }

  static async create(data: Omit<Signalement, "idSignalement">) {
    try {
      const result = await SignalementRepository.create(data);
      return result;
    } catch (error) {
      console.error("Erreur lors de la création du signalement:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<Signalement, "idSignalement">>
  ): Promise<void> {
    try {
      await SignalementRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour du signalement ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await SignalementRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression du signalement ${id}:`, error);
      throw error;
    }
  }
}
