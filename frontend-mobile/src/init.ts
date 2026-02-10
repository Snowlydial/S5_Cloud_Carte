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
import { CompteService } from "./services/Compte.service";
import { signInWithEmailAndPassword, signOut } from "firebase/auth";
import { auth } from "@/firebase";
import { SupabaseService } from "./services/supabase.service";


(async () => {


  const userCredential = await signInWithEmailAndPassword(
    auth,
    "a@gmail.com",
    "aaaaaa"
  );


  // await CloudinaryService.uploadImage ();
  // try {
  //   await SupabaseService.uploadImage();
  // } catch (err) {
  //   console.error("Erreur upload Supabase :", err);
  // }


  // await ProfilSeeder.seed();
  // await TypeSignalementSeeder.seed();
  // await SignalementSeeder.seed();
  // await StatusSeeder.seed();
  // await EntrepriseSeeder.seed();
  // await ProblemeSeeder.seed();
  // await ProblemeStatusSeeder.seed();


  console.log("Seeding terminé ✅");
  await signOut(auth);
  process.exit();
})();
