import { ProblemeStatusRepository } from "@/repositories/ProblemeStatusRepository";
import { ProblemeStatus } from "@/models/ProblemeStatus";

type ProblemeStatusWithDate = ProblemeStatus & { dateStatus?: Date | string };

export function getLastStatusLocal(statusList: ProblemeStatusWithDate[]): ProblemeStatus | null {
  if (!statusList.length) return null;

  return statusList
    .slice()
    .sort((a, b) => {
      const da = a.dateStatus ? new Date(a.dateStatus).getTime() : 0;
      const db = b.dateStatus ? new Date(b.dateStatus).getTime() : 0;
      return db - da;
    })[0];
}

export class ProblemeStatusService {
  static async getAll(): Promise<ProblemeStatus[]> {
    try {
      return await ProblemeStatusRepository.getAll();
    } catch (error) {
      console.error("Erreur lors de la récupération des problème-status:", error);
      throw error;
    }
  }

  static async getById(id: string): Promise<ProblemeStatus | null> {
    try {
      return await ProblemeStatusRepository.getById(id);
    } catch (error) {
      console.error(`Erreur lors de la récupération du problème-status ${id}:`, error);
      throw error;
    }
  }

  static async getByIdProbleme(idProbleme: string): Promise<ProblemeStatus[]> {
    try {
      return await ProblemeStatusRepository.getByIdProbleme(idProbleme);
    } catch (error) {
      console.error(`Erreur lors de la récupération des status du problème ${idProbleme}:`, error);
      throw error;
    }
  }

  static async getLastByIdProbleme(idProbleme: string): Promise<ProblemeStatus | null> {
    try {
      const allStatus = await ProblemeStatusRepository.getAll();
      const filtered = allStatus.filter(status => status.idProbleme === idProbleme);
      return getLastStatusLocal(filtered);
    } catch (error) {
      console.error(`Erreur lors de la récupération du dernier status (local) du problème ${idProbleme}:`, error);
      throw error;
    }
  }

 

  static async getByIdStatus(idStatus: string): Promise<ProblemeStatus[]> {
    try {
      return await ProblemeStatusRepository.getByIdStatus(idStatus);
    } catch (error) {
      console.error(`Erreur lors de la récupération des problèmes avec le status ${idStatus}:`, error);
      throw error;
    }
  }

  static async create(data: Omit<ProblemeStatus, "idProblemeStatus">): Promise<string> {
    try {
      return await ProblemeStatusRepository.create(data);
    } catch (error) {
      console.error("Erreur lors de la création du problème-status:", error);
      throw error;
    }
  }

  static async update(
    id: string,
    data: Partial<Omit<ProblemeStatus, "idProblemeStatus">>
  ): Promise<void> {
    try {
      await ProblemeStatusRepository.update(id, data);
    } catch (error) {
      console.error(`Erreur lors de la mise à jour du problème-status ${id}:`, error);
      throw error;
    }
  }

  static async delete(id: string): Promise<void> {
    try {
      await ProblemeStatusRepository.delete(id);
    } catch (error) {
      console.error(`Erreur lors de la suppression du problème-status ${id}:`, error);
      throw error;
    }
  }
}
