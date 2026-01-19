import { CompteSeeder } from "./seeders/CompteSeeder";
import { ProfilSeeder } from "./seeders/ProfilSeeder";
import { UserSeeder } from "./seeders/UserSeed";

(async () => {
  await UserSeeder.seed();
  await ProfilSeeder.seed();
  await CompteSeeder.seed();
  console.log("Seeding terminé ✅");
  process.exit();
})();
