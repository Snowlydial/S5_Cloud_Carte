import { CompteSeeder } from "./seeders/CompteSeeder";
import { EntrepriseSeeder } from "./seeders/EntrepriseSeeder";
import { ProblemeSeeder } from "./seeders/ProblemeSeeder";
import { ProfilSeeder } from "./seeders/ProfilSeeder";
import { SignalementSeeder } from "./seeders/SignalementSeeder";
import { StatusSeeder } from "./seeders/StatusSeeder";
import { TypeSignalementSeeder } from "./seeders/TypeSignalementSeeder";
import { UserSeeder } from "./seeders/UserSeed";

(async () => {
  await UserSeeder.seed();
  await ProfilSeeder.seed();
  // await CompteSeeder.seed();
  await SignalementSeeder.seed();
  await StatusSeeder.seed();
  await EntrepriseSeeder.seed();
  await TypeSignalementSeeder.seed();
  await ProblemeSeeder.seed();
  console.log("Seeding terminé ✅");
  process.exit();
})();
