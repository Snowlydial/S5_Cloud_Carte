//?=== AUTHENTICATION SERVICE (Using Axios)
//*-- Handles all auth-related API calls

import axios from 'axios';
import { AUTH_ENDPOINTS } from '../config/constants';
import { auth, isFirebaseConfigured } from '../config/firebase';

//*-- Configure axios defaults
axios.defaults.headers.post['Content-Type'] = 'application/json';

//*-- Check if we have internet connection
const checkOnlineStatus = () => {
    return navigator.onLine;
};

//*-- MOCK: Simulate backend response (remove when backend ready)
const mockApiCall = (endpoint, data) => {
    return new Promise((resolve) => {
        setTimeout(() => {
            console.log(`[MOCK API] ${endpoint}`, data);

            if (endpoint.includes('login')) {
                resolve({
                    data: {
                        success: true,
                        user: {
                            email: data.email,
                            role: 'MANAGER',
                            firebaseUid: 'mock-uid-123'
                        },
                        token: 'mock-jwt-token'
                    }
                });
            } else if (endpoint.includes('register')) {
                resolve({
                    data: {
                        success: true,
                        user: {
                            email: data.email,
                            role: 'USER',
                            firebaseUid: data.firebaseUid || null
                        }
                    }
                });
            }
        }, 500);
    });
};

//?=== LOGIN FUNCTION (Hybrid: Firebase + Backend)
export const login = async (email, password) => {
    const isOnline = checkOnlineStatus();

    try {
        //*-- Try Firebase authentication if online and configured
        if (isOnline && isFirebaseConfigured && auth) {
            const { signInWithEmailAndPassword } = await import('firebase/auth');
            const userCredential = await signInWithEmailAndPassword(auth, email, password);
            const idToken = await userCredential.user.getIdToken();
            console.log("Firebase ID Token:", idToken);
            //*-- Send Firebase token to backend
            // TODO: Uncomment when backend ready
            const response = await axios.post(AUTH_ENDPOINTS.LOGIN, {
                email,
                password,
                firebaseUid: userCredential.user.uid,
            });
            console.log("Backend login response:", response);
            return response.data;

            //*-- MOCK version (remove when backend ready)
            // const response = await mockApiCall(AUTH_ENDPOINTS.LOGIN, {
            //     email,
            //     password,
            //     firebaseToken: idToken
            // });
        }

        //*-- Fallback: Local authentication
        // TODO: Uncomment when backend ready
        const response = await axios.post(AUTH_ENDPOINTS.LOGIN, {
            email,
            password
            });
            return response.data;
           
            
        return response.data;

    } catch (error) {
        console.error('Login error:', error);

        //*-- Handle axios errors
        if (error.response) {
            // Backend responded with error
            throw new Error(error.response.data.message || 'Échec de connexion');
        } else if (error.request) {
            // Request made but no response (network issue)
            throw new Error('Erreur réseau. Vérifiez votre connexion.');
        } else {
            // Firebase or other error
            throw new Error(error.message || 'Échec de connexion');
        }
    }
};

//?=== REGISTER FUNCTION (Hybrid: Firebase + Backend)
export const register = async (email, password) => {
    const isOnline = checkOnlineStatus();
    let firebaseUid = null;
    let idToken = null;
    try {
        //*-- Try Firebase registration if online and configured
        if (isOnline && isFirebaseConfigured && auth) {
            const { createUserWithEmailAndPassword } = await import('firebase/auth');
            console.log("auth", auth);
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            firebaseUid = userCredential.user.uid;
            idToken = await userCredential.user.getIdToken();
        }

        //*-- Always register in backend (with or without Firebase UID)
        // TODO: Uncomment when backend ready
        const response = await axios.post(AUTH_ENDPOINTS.REGISTER, {
            email,
            password,
            firebaseUid : idToken
            });
            /*
            return response.data;
        */

        //*-- MOCK version (remove when backend ready)
        // const response = await mockApiCall(AUTH_ENDPOINTS.REGISTER, {
        //     email,
        //     password,
        //     firebaseUid
        // });
        return response.data;

    } catch (error) {
        console.error('Registration error:', error);

        //*-- Handle axios errors
        if (error.response) {
            throw new Error(error.response.data.message || 'Échec d\'inscription');
        } else if (error.request) {
            throw new Error('Erreur réseau. Vérifiez votre connexion.');
        } else {
            throw new Error(error.message || 'Échec d\'inscription');
        }
    }
};

//?=== LOGOUT FUNCTION
export const logout = async () => {
    try {
        //*-- Sign out from Firebase if configured
        if (isFirebaseConfigured && auth) {
            const { signOut } = await import('firebase/auth');
            await signOut(auth);
        }

        //*-- Call backend logout endpoint if needed
        // TODO: Uncomment when backend ready
        // await axios.post(AUTH_ENDPOINTS.LOGOUT);

        return { success: true };
    } catch (error) {
        console.error('Logout error:', error);
        throw new Error('Échec de déconnexion');
    }
};

//?=== AXIOS INTERCEPTOR (Add JWT token to all requests)
//*-- Uncomment when backend implements JWT authentication
// Avoids attaching auth token to every request after login
/*
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);
*/

//?=== HELPER: Example of axios calls (reference for later)
/*
//* GET request
const getUser = async (userId) => {
    const response = await axios.get(`${API_BASE_URL}/users/${userId}`);
    return response.data;
};

//* POST request
const createUser = async (userData) => {
    const response = await axios.post(`${API_BASE_URL}/users`, userData);
    return response.data;
};

//* PUT request
const updateUser = async (userId, userData) => {
    const response = await axios.put(`${API_BASE_URL}/users/${userId}`, userData);
    return response.data;
};

//* DELETE request
const deleteUser = async (userId) => {
    const response = await axios.delete(`${API_BASE_URL}/users/${userId}`);
    return response.data;
};
*/