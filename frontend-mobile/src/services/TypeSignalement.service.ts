import { TypeSignalementRepository } from "@/repositories/TypeSignalementRepository";
import { TypeSignalement } from "@/models/TypeSignalement";

export class TypeSignalementService {
  static async getAll(): Promise<TypeSignalement[]> {
    try {
      return await TypeSignalementRepository.findAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des types de signalement:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<TypeSignalement | null> {
    try {
      return await TypeSignalementRepository.findById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération du type de signalement ${id}:`, error);
      throw error;
    }
  }

  static async getByNom(nom: string): Promise<TypeSignalement | null> {
    try {
      return await TypeSignalementRepository.findByNom(nom);
    } catch (error) {
      console.error(`Erreur lors de la récupération du type de signalement '${nom}':`, error);
      throw error;
    }
  }

  static async create(data: Omit<TypeSignalement, "idTypeSignalement">) {
    try {
      const result = await TypeSignalementRepository.create(data);
      return result;
    } catch (error) {
      console.error("Erreur lors de la création du type de signalement:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<TypeSignalement, "idTypeSignalement">>
  ): Promise<void> {
    try {
      await TypeSignalementRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour du type de signalement ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await TypeSignalementRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression du type de signalement ${id}:`, error);
      throw error;
    }
  }
}
