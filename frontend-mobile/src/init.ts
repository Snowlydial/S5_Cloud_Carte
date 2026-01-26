import { EntrepriseRepository } from "./repositories/EntrepriseRepository";
import { CompteSeeder } from "./seeders/CompteSeeder";
import { EntrepriseSeeder } from "./seeders/EntrepriseSeeder";
import { ProblemeSeeder } from "./seeders/ProblemeSeeder";
import { ProblemeStatusSeeder } from "./seeders/ProblemeStatusSeeder";
import { ProfilSeeder } from "./seeders/ProfilSeeder";
import { SignalementSeeder } from "./seeders/SignalementSeeder";
import { StatusSeeder } from "./seeders/StatusSeeder";
import { TypeSignalementSeeder } from "./seeders/TypeSignalementSeeder";
import { UserSeeder } from "./seeders/UserSeed";

(async () => {
  await UserSeeder.seed();
  await ProfilSeeder.seed();
  // await CompteSeeder.seed();
  await TypeSignalementSeeder.seed();
  await SignalementSeeder.seed();
  await StatusSeeder.seed();
  await EntrepriseSeeder.seed();
  await ProblemeSeeder.seed();
  await ProblemeStatusSeeder.seed();  

  const entreprises = await EntrepriseRepository.getAll();
  console.log("Entreprises dans la base de données:", entreprises);

  console.log("Seeding terminé ✅");
  process.exit();
})();
