
import { createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut } from "@firebase/auth";
import { ApiResponse } from "@/types/apiResponse";
import { Profil } from "@/models/Profil";
import { ProfilRepository } from "@/repositories/ProfilRepository";
import { Compte } from "@/models/Compte";
import { CompteRepository } from "@/repositories/CompteRepository";
import { auth } from "@/firebase";

export async function loginService(email: string, password: string): Promise<ApiResponse> {
    const compte = await CompteRepository.findByEmail(email);

    try {
        if (compte && compte.tentative && compte.tentative >= 10) {
            throw new Error("Compte verrouillé en raison de trop nombreuses tentatives de connexion échouées.");
        }
        // const testEmail = "testuser123@example.com";
        // const testPassword = "123456";
        // const userCredential = await signInWithEmailAndPassword(auth, testEmail, testPassword);
        const userCredential = await signInWithEmailAndPassword(auth, email, password);

        console.log("Compte trouvé lors de la connexion réussie :", compte);
        if (compte){
            localStorage.setItem("compteId", compte.id!);
        }

        // const profil: Profil = {
        //     id: "admin",
        //     nom: "Administrateur"
        // };

        // const profils : Profil[] = await ProfilRepository.getAll();
        // console.log("Profils récupérés lors de la connexion :",     profils);
        localStorage.setItem("loginTime", Date.now().toString());
        return {
            success: true,
            code: 200,
            message: "Connexion réussie",
            data: {
                uid: userCredential.user.uid,
                email: userCredential.user.email
            }
        };
    } catch (err: any) {

        console.log("Compte trouvé lors de l'échec de connexion :", compte);
        if (compte && compte.mdp !== password) {
            const tentative = compte.tentative || 0;

            await CompteRepository.update(compte.id!, { tentative: tentative + 1 });

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

        const profil: Profil | null = await ProfilRepository.getByName("utilisateur");
        if (!profil) {
            throw new Error("Profil 'utilisateur' non trouvé");
        }
        if (!profil.id) {
            throw new Error("Profil ID non défini");
        }

        const compte: Compte = {
            email: email,
            nom: email,
            mdp: password,
            profilId: profil.id
        };
        await CompteRepository.create(compte);



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

