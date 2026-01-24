//?=== AUTHENTICATION CONTEXT: shares auth state across all components
import React, { createContext, useState, useContext, useEffect } from 'react';
import { login as loginService, logout as logoutService, register as registerService } from '../services/authService';

//*-- Create the context (the "container" for shared data)
const AuthContext = createContext();

//*-- Custom hook to use auth in any component
//*-- Usage: const { user, login, logout } = useAuth();
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};

//?=== AUTH PROVIDER COMPONENT
//*-- Wrap your app with this to share auth state everywhere
export const AuthProvider = ({ children }) => {
    //*-- State: stores current user data
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    //*-- On mount: check if user was logged in (from localStorage)
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                setUser(JSON.parse(storedUser));
            } catch (error) {
                console.error('Failed to parse stored user:', error);
                localStorage.removeItem('user');
            }
        }
        setLoading(false);
    }, []);

    //*-- Login function: calls service, then updates state
    const login = async (email, password) => {
        try {
            const response = await loginService(email, password);
            const userData = response;

            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
            console.log("User logged in:", userData);
            return { success: true, data: userData };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    //*-- Register function
    const register = async (email, password,roleValue) => {
        try {
            const response = await registerService(email, password,roleValue);
            // Don't auto-login after register, let user login manually
            return { success: true };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    //*-- Logout function: clears state and storage
    const logout = async () => {
        try {
            await logoutService();
            setUser(null);
            localStorage.removeItem('user');
            return { success: true };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    //*-- Check if user has specific role
    const hasRole = (role) => {
        return user && user.role === role;
    };

    //*-- Value provided to all children components
    const value = {
        user,           // Current user object (null if not logged in)
        loading,        // True while checking localStorage
        login,          // Function to login
        register,       // Function to register
        logout,         // Function to logout
        hasRole,        // Function to check user role
        isAuthenticated: !!user  // Boolean: is user logged in?
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};