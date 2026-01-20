import { StatusRepository } from "@/repositories/StatusRepository";
import { Status } from "@/models/Status";

export class StatusService {
  static async getAll(): Promise<Status[]> {
    try {
      return await StatusRepository.getAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des status:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<Status | null> {
    try {
      return await StatusRepository.getById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération du status ${id}:`, error);
      throw error;
    }
  }

  static async getByName(nom: string): Promise<Status | null> {
    try {
      return await StatusRepository.getByName(nom);
    } catch (error) {
      console.error(`Erreur lors de la récupération du status ${nom}:`, error);
      throw error;
    }
  }

  static async create(data: Omit<Status, "id">): Promise<string> {
    try {
      return await StatusRepository.create(data);
    } catch (error) {
      console.error("Erreur lors de la création du status:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<Status, "id">>
  ): Promise<void> {
    try {
      await StatusRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour du status ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await StatusRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression du status ${id}:`, error);
      throw error;
    }
  }
}
