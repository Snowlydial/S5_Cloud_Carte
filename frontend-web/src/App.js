//?=== MAIN APP WITH ROUTING

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import UserManagementPage from './pages/UserManagementPage';
import MapPage from './pages/MapPage';
import SignalementPage from './pages/SignalementPage';
import './App.css';

//*-- Protected Route: redirects to login if not authenticated
const ProtectedRoute = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();
    
    if (loading) {
        return <div className="loading">Chargement...</div>;
    }
  
    return isAuthenticated ? children : <Navigate to="/login" />;
};

//*-- Public Route: redirects to dashboard if already authenticated
const PublicRoute = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();
    
    if (loading) {
        return <div className="loading">Chargement...</div>;
    }
    
    return !isAuthenticated ? children : <Navigate to="/dashboard" />;
};

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    {/* Redirect root to login */}
                    <Route path="/" element={<Navigate to="/login" />} />
                    
                    {/* Public routes */}
                    <Route 
                        path="/login" 
                        element={
                        <PublicRoute>
                            <LoginPage />
                        </PublicRoute>
                        } 
                    />
                    <Route 
                        path="/register" 
                        element={
                        <PublicRoute>
                            <RegisterPage />
                        </PublicRoute>
                        } 
                    />
                    
                    {/* Protected routes */}
                    <Route 
                        path="/dashboard" 
                        element={
                        <ProtectedRoute>
                            <DashboardPage />
                        </ProtectedRoute>
                        } 
                    />
                    <Route 
                        path="/users" 
                        element={
                        <ProtectedRoute>
                            <UserManagementPage />
                        </ProtectedRoute>
                        } 
                    />

                    <Route 
                        path="/map" 
                        element={
                        <ProtectedRoute>
                            <MapPage />
                        </ProtectedRoute>
                        } 
                    />

                    <Route 
                        path="/signalements" 
                        element={
                        <ProtectedRoute>
                            <SignalementPage />
                        </ProtectedRoute>
                        } 
                    />
                    

                    {/* 404 fallback */}
                    <Route path="*" element={<Navigate to="/login" />} />
                
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;