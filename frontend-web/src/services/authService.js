//?=== AUTHENTICATION SERVICE
//*-- Handles all auth-related API calls

import { AUTH_ENDPOINTS } from '../config/constants';
import { auth, isFirebaseConfigured } from '../config/firebase';

//*-- Check if we have internet connection
const checkOnlineStatus = () => {
    return navigator.onLine;
};

//*-- REAL: Real fetch template (use this when backend ready)
/*
const realFetch = async (url, options = {}) => {
    const response = await fetch(url, {
        method: options.method || 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        body: JSON.stringify(options.body)
    });
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
};
*/

//*-- MOCK: Simulate backend response (remove when backend ready)
const mockApiCall = (endpoint, data) => {
    return new Promise((resolve) => {
        setTimeout(() => {
            console.log(`[MOCK API] ${endpoint}`, data);
            
            if (endpoint.includes('login')) {
                resolve({
                    success: true,
                    user: {
                        email: data.email,
                        role: 'MANAGER',
                        firebaseUid: 'mock-uid-123'
                    },
                    token: 'mock-jwt-token'
                });
            } else if (endpoint.includes('register')) {
                resolve({
                    success: true,
                    user: {
                        email: data.email,
                        role: 'USER',
                        firebaseUid: data.firebaseUid || null
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
            
            //*-- Send Firebase token to backend
            // TODO: Replace mockApiCall with real fetch
            const response = await mockApiCall(AUTH_ENDPOINTS.LOGIN, {
                email,
                firebaseToken: idToken
            });
            
            return response;
        }
        
        //*-- Fallback: Local authentication
        // TODO: Replace mockApiCall with real fetch
        const response = await mockApiCall(AUTH_ENDPOINTS.LOGIN, {
            email,
            password
        });
        
        return response;
        
    } catch (error) {
        console.error('Login error:', error);
        throw new Error(error.message || 'Échec de connexion');
    }
};

//?=== REGISTER FUNCTION (Hybrid: Firebase + Backend)
export const register = async (email, password) => {
    const isOnline = checkOnlineStatus();
    let firebaseUid = null;
    
    try {
        //*-- Try Firebase registration if online and configured
        if (isOnline && isFirebaseConfigured && auth) {
            const { createUserWithEmailAndPassword } = await import('firebase/auth');
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
                firebaseUid = userCredential.user.uid;
        }
        
        //*-- Always register in backend (with or without Firebase UID)
        // TODO: Replace mockApiCall with real fetch
        const response = await mockApiCall(AUTH_ENDPOINTS.REGISTER, {
            email,
            password,
            firebaseUid
        });
        
        return response;
        
    } catch (error) {
        console.error('Registration error:', error);
        throw new Error(error.message || 'Échec d\'inscription');
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
        
        // TODO: Call backend logout endpoint if needed
        // await fetch(AUTH_ENDPOINTS.LOGOUT, { method: 'POST' });
        
        return { success: true };
    } catch (error) {
        console.error('Logout error:', error);
        throw new Error('Échec de déconnexion');
    }
};
