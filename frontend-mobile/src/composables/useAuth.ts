// src/composables/useAuth.ts
import { ref } from "vue";
import type { User } from "firebase/auth";
import { ApiResponse } from "@/types/apiResponse";
import { loginService, logoutService, signinService } from "@/services/auth.service";
import { Util } from "@/utils/util";
import { getErrorMessage, getSuccessMessage } from "@/utils/messageUtil";
import { Recap } from "@/types/Recap";
import { CompteService } from "@/services/Compte.service";

const user = ref<User | null>(null);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const loading = ref(false);


const useAuth = () => {

    const login = async (email: string, password: string) => {
        loading.value = true;
        success.value = null;
        error.value = null;
        const response: ApiResponse = await loginService(email, password);
        console.log("Erreur de login :", response);
        if (response.success) {
            user.value = {
                uid: response.data?.uid || '',
                email: response.data?.email || ''
            } as User;
            success.value = getSuccessMessage(response);
        } else {
            error.value = getErrorMessage(response);
        }
        loading.value = false;
    };

    const signin = async (email: string, password: string) => {
        loading.value = true;
        const response: ApiResponse = await signinService(email, password);
        // console.log(response);
        if (response.success) {
            user.value = {
                uid: response.data?.uid || '',
                email: response.data?.email || ''
            } as User;
            success.value = getSuccessMessage(response);
        } else {
            error.value = getErrorMessage(response);
        }
        loading.value = false;
    };

    const fiche = (idUtilisateur: string | number) => {
        console.log(`Récupération de la fiche pour : ${idUtilisateur}`);
    };

    const logout = async () => {
        loading.value = true;
        const response = await logoutService();
        if (response.success) {
            user.value = null; // On vide l'utilisateur
            success.value = "Déconnexion réussie";
            error.value = null;
            console.log("deconnexion reussi");
        } else {
            error.value = getErrorMessage(response);
            console.log("echec de deconnexion");

        }
        loading.value = false;
    };

    // N'oubliez pas d'ajouter logout au return
    return { user, login, signin, logout, fiche, loading, error, success };
};

export default useAuth;


