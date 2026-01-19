// src/composables/useAuth.ts
import { ref } from "vue";
import type { User } from "firebase/auth";
import { ApiResponse } from "@/types/apiResponse";
import { loginService, signinService } from "@/services/auth.service";
import { Util } from "@/utils/util";
import { getErrorMessage, getSuccessMessage } from "@/utils/messageUtil";

const user = ref<User | null>(null);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const loading = ref(false);

const useAuth = () => {

    const login = async (email: string, password: string) => {
        loading.value = true;
        const response: ApiResponse = await loginService(email, password);
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

    return { user, login, signin, fiche, loading, error, success };
};

export default useAuth;


