import { CompteSeeder } from "./seeders/CompteSeeder";
import { ProfilSeeder } from "./seeders/ProfilSeeder";
import { TypeSignalementSeeder } from "./seeders/TypeSignalementSeeder";
import { UserSeeder } from "./seeders/UserSeed";

(async () => {
  await UserSeeder.seed();
  await ProfilSeeder.seed();
  await CompteSeeder.seed();
  await TypeSignalementSeeder.seed();
  console.log("Seeding terminé ✅");
  process.exit();
})();
