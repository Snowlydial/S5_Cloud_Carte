import { ref } from "vue";
import type { User } from "firebase/auth";
import { loginService, signinService } from "@/services/auth.service";
import { ApiResponse } from "@/types/apiResponse";
import { TypeSignalement } from "@/models/TypeSignalement";
import { TypeSignalementService } from "@/services/TypeSignalement.service";

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
        const result: TypeSignalement[] =  await TypeSignalementService.getAll();
        typesSignalement.value = result;
        loading.value = false;
    };

    return { typesSignalement, getListeTypeSignalement, loading, error , success };
};

export default useTypeSignalement;
