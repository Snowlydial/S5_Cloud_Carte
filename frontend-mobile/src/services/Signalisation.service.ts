import { auth } from "@/firebase";
import { ApiResponse } from "@/types/apiResponse";
import { createUserWithEmailAndPassword, signInWithEmailAndPassword } from "firebase/auth";
// import { createUserWithEmailAndPassword, signInWithEmailAndPassword } from "@firebase/auth";

export async function signaler(email: string, password: string): Promise<ApiResponse> {
    try {
        const testEmail = "testuser123@example.com";
        const testPassword = "123456";
        // const userCredential = await signInWithEmailAndPassword(auth, testEmail, testPassword);
        const userCredential = await signInWithEmailAndPassword(auth, email, password);

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
        const testEmail = "testuser123@example.com";
        const testPassword = "123456";
        const userCredential = await createUserWithEmailAndPassword(auth, testEmail, testPassword);

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

