
import { createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut } from "@firebase/auth";
import { ApiResponse } from "@/types/apiResponse";
import { Profil } from "@/models/Profil";
import { ProfilRepository } from "@/repositories/ProfilRepository";
import { Compte } from "@/models/Compte";
import { CompteRepository } from "@/repositories/CompteRepository";
import { auth, initPushNotifications } from "@/firebase";
import { CompteService } from "./Compte.service";
import { ProfilService } from "./Profil.service";
import { Preferences } from '@capacitor/preferences';
import { ConfigurationRepository } from "@/repositories/ConfigurationRepository";

export async function loginService(email: string, password: string): Promise<ApiResponse> {
    async function checkInternet(timeoutMs = 5000): Promise<boolean> {
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), timeoutMs);




        try {
            const res = await fetch("https://1.1.1.1/cdn-cgi/trace", {
                cache: "no-store",
                signal: controller.signal
            });

            return res.ok;
        } catch (e) {
            return false;
        } finally {
            clearTimeout(timeout);
        }
    }
    // if (!isOnline){
    //     throw new Error("Aucune connexion internet. Veuillez vérifier votre connexion et réessayer.");
    // }

    let compte = null;
    try {
        const isOnline = await checkInternet();
        if (!isOnline) {
            console.log("totototot")
            throw new Error("Aucune connexion internet. Veuillez vérifier votre connexion et réessayer.");
        }
        compte = await CompteRepository.findByEmail(email);
        if (!compte) {
            throw new Error("Compte non trouvé avec cet email.");
        }
        let tentative = 1;
        const allConfig = await ConfigurationRepository.findAll();

        if (allConfig.length > 0) {
            tentative = allConfig[0].tentative_max;
        }

        console.log("Configurations récupérées lors de la connexion :", allConfig[0]);
        console.log("Tentative max autorisée :", tentative);
        if ((compte && compte.tentative && compte.tentative >= tentative) || (compte && compte.isBlocked)) {

            await CompteRepository.update(compte.idCompte!, { isBlocked: true });

            throw new Error("Compte verrouillé en raison de trop nombreuses tentatives de connexion échouées.");
        }
        if (compte.profil === undefined || compte.profil === null || compte.profil === "") {
            throw new Error("Le profil du compte n'est pas défini.");
        }
        const profilCompte = compte ? await ProfilService.getById(compte.profil) : null;
        console.log("Profil du compte lors de la connexion :", profilCompte);
        if (!profilCompte || profilCompte.nom !== "USER") {
            throw new Error("Le profil du compte n'est pas autorisé à se connecter.");
        }
        // const testEmail = "testuser123@example.com";
        // const testPassword = "123456";
        // const userCredential = await signInWithEmailAndPassword(auth, testEmail, testPassword);


        // const userCredential = await signInWithEmailAndPassword(auth, email, password);

        console.log("Compte trouvé lors de la connexion réussie :", compte);
        if (compte.password !== password) {
            throw new Error("mot de passe incorrect.");
        }
        if (compte) {

            localStorage.setItem("compteId", compte.idCompte!);
            await Preferences.set({ key: 'compteId', value: compte.idCompte! });
        }

        // const recap = await CompteService.getRecap();
        // console.log ("Récapitulatif utilisateur après connexion :", recap);

        // const profil: Profil = {
        //     id: "admin",
        //     nom: "Administrateur"
        // };

        // const profils : Profil[] = await ProfilRepository.getAll();
        // console.log("Profils récupérés lors de la connexion :",     profils);
        const token = await initPushNotifications();
        if (token && compte) {
            compte.fcmTokens = compte.fcmTokens ? [...compte.fcmTokens, token] : [token];
            const tokens = compte.fcmTokens || [];
            compte.fcmTokens = Array.from(new Set([...tokens, token]));
            await CompteRepository.update(compte.idCompte!, { fcmTokens: compte.fcmTokens });
        }


        // localStorage.setItem("loginTime", Date.now().toString());
        // await Preferences.set({ key: 'loginTime', value: Date.now().toString() });

        return {
            success: true,
            code: 200,
            message: "Connexion réussie",
            data: {
                uid: compte.firebaseId,
                email: compte.email
            }
        };
    } catch (err: any) {

        console.log("Compte trouvé lors de l'échec de connexion :", compte);
        const isOnline = await checkInternet();

        if (compte && compte.password !== password && isOnline) {
            const tentative = compte.tentative || 0;

            await CompteRepository.update(compte.idCompte!, { tentative: tentative + 1 });

        }


        return {
            success: false,
            code: 400,
            message: "Échec de la connexion",
            error: {
                type: "AUTH_ERROR",
                details: err.message
            }
        };
    }
}

export async function signinService(email: string, password: string): Promise<ApiResponse> {
    try {
        // const testEmail = "testuser123@example.com";
        // const testPassword = "123456";



        const userCredential = await createUserWithEmailAndPassword(auth, email, password);

        const profil: Profil | null = await ProfilRepository.getByName("USER");
        if (!profil) {
            throw new Error("Profil 'utilisateur' non trouvé");
        }
        // if (!profil.profil) {
        //     throw new Error("Profil ID non défini");
        // }

        // const compte: Compte = {
        //     email: email,
        //     nom: email,
        //     mdp: password,
        //     profilId: profil.id
        // };
        // await CompteRepository.create(compte);



        return {
            success: true,
            code: 200,
            message: "Inscription réussie",
            data: {
                uid: userCredential.user.uid,
                email: userCredential.user.email
            }
        };
    } catch (err: any) {
        return {
            success: false,
            code: 400,
            message: "Échec de l'inscription",
            error: {
                type: "AUTH_ERROR",
                details: err.message
            }
        };
    }
}

export async function logoutService(): Promise<ApiResponse> {
    try {
        // await signOut(auth);

        // localStorage.removeItem("compteId");
        // localStorage.removeItem("loginTime");

        await Preferences.remove({ key: 'compteId' });
        // await Preferences.remove({ key: 'loginTime' });

        return {
            success: true,
            code: 200,
            message: "Déconnexion réussie"
        };
    } catch (err: any) {
        return {
            success: false,
            code: 400,
            message: "Échec de la déconnexion",
            error: {
                type: "AUTH_ERROR",
                details: err.message
            }
        };
    }
}

