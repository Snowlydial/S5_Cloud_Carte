import { SignalementService } from "@/services/Signalement.service";
import { Signalement } from "./Signalement";
import { ProblemeRepository } from "@/repositories/ProblemeRepository";
import { Probleme } from "./Probleme";

export interface SignalementProbleme extends Signalement {
  // Détails Type
  typeNom?: string;
  typeIcon?: string;

  // Détails Problème
  idProbleme?: string;
  surfaceM2?: number;
  budget?: number;
  dateProbleme?: Date;

  // Détails Entreprise
  nomEntreprise?: string;

  // Détails Status (Le plus récent)
  statusActuel?: string;
  statusDate?: Date;

  niveau?: number;
}
