import { ref } from "vue";
import type { User } from "firebase/auth";
import { loginService, signinService } from "@/services/auth.service";
import { ApiResponse } from "@/types/apiResponse";
import { getErrorMessage } from "@/utils/getErrorMessage";
import { getSuccessMessage } from "@/utils/getSuccessMessage";

export interface TypeSignalement {
    idTypeSignalement: number;
    nom: string;
}

const typesSignalement = ref<TypeSignalement[] | null>(null);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const loading = ref(false);

const useTypeSignalement = () => {

    const getListeTypeSignalement = async () => {
        loading.value = true;
        // const response: ApiResponse = await loginService(email, password);
        
        // if (response.success) {
        //     typesSignalement.value = response.data?.typesSignalement || [];
        //     success.value = getSuccessMessage (response);
        // } else {
        //     error.value =  getErrorMessage (response);
        // }
        const result: TypeSignalement[] = [
                { idTypeSignalement: 1, nom: "Panne d'éclairage public" },
                { idTypeSignalement: 2, nom: "Nid de poule" },
                { idTypeSignalement: 3, nom: "Accident" }
            ];
        typesSignalement.value = result;
        loading.value = false;
    };

    return { typesSignalement, getListeTypeSignalement, loading, error , success };
};

export default useTypeSignalement;
