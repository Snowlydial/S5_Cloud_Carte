
import { createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut } from "@firebase/auth";
import { ApiResponse } from "@/types/apiResponse";
import { Profil } from "@/models/Profil";
import { ProfilRepository } from "@/repositories/ProfilRepository";
import { Compte } from "@/models/Compte";
import { CompteRepository } from "@/repositories/CompteRepository";
import { auth, initPushNotifications } from "@/firebase";
import { CompteService } from "./Compte.service";
import { ProfilService } from "./Profil.service";
export async function loginService(email: string, password: string): Promise<ApiResponse> {
    async function checkInternet(timeoutMs = 5000): Promise<boolean> {
        // In browser environments, use navigator.onLine as primary check
        // The fetch approach fails due to CORS restrictions
        if (!navigator.onLine) {
            return false;
        }
        return true;
    }


    // const isOnline = navigator.onLine;
    // if (!isOnline){
    //     throw new Error("Aucune connexion internet. Veuillez vérifier votre connexion et réessayer.");
    // }
    
    let compte = null;
    try {
        const isOnline = await checkInternet();
        if (!isOnline) {
            console.log ("totototot")
            throw new Error("Aucune connexion internet. Veuillez vérifier votre connexion et réessayer.");
        }
        compte = await CompteRepository.findByEmail(email);
        if (!compte) {
            throw new Error("Compte non trouvé avec cet email.");
        }
        if ((compte && compte.tentative && compte.tentative >= 3) || (compte && compte.isBlocked)) {

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
        if (compte) {
            localStorage.setItem("compteId", compte.idCompte!);
        }

        // const recap = await CompteService.getRecap();
        // console.log ("Récapitulatif utilisateur après connexion :", recap);

        // const profil: Profil = {
        //     id: "admin",
        //     nom: "Administrateur"
        // };

        // const profils : Profil[] = await ProfilRepository.getAll();
        // console.log("Profils récupérés lors de la connexion :",     profils);
        const token = await initPushNotifications ();
        if (token && compte) {
            compte.fcmTokens = compte.fcmTokens ? [...compte.fcmTokens, token] : [token];
            const tokens = compte.fcmTokens || [];
            compte.fcmTokens = Array.from(new Set([...tokens, token]));
            await CompteRepository.update(compte.idCompte!, { fcmTokens: compte.fcmTokens });
        }


        localStorage.setItem("loginTime", Date.now().toString());
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

        if (compte && compte.mdp !== password && isOnline) {
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
        await signOut(auth);

        localStorage.removeItem("compteId");
        localStorage.removeItem("loginTime");

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

