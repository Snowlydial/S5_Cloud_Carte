//?=== DASHBOARD PAGE

import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../styles/Dashboard.css';

const DashboardPage = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <h1>Tableau de bord</h1>
                <div className="user-info">
                    <span>{user?.email}</span>
                    <span className="role-badge">{user?.role}</span>
                    <button onClick={handleLogout} className="btn-secondary">
                        Déconnexion
                    </button>
                </div>
            </header>

            <div className="dashboard-content">
                <div className="welcome-card">
                    <h2>Bienvenue, {user?.email}</h2>
                    <p>Système de gestion des travaux routiers - Antananarivo</p>
                </div>

                {/* Placeholder for future modules */}
                <div className="modules-grid">
                    <div className="module-card">
                        <h3>Carte & Signalements</h3>
                        <p>Visualiser les problèmes routiers</p>
                        <button className="btn-primary" disabled>Bientôt disponible</button>
                    </div>

                    {user?.role === 'MANAGER' && (
                        <>
                            <div className="module-card">
                                <h3>Gestion Utilisateurs</h3>
                                <p>Gérer les comptes bloqués</p>
                                <button className="btn-primary" disabled>Bientôt disponible</button>
                            </div>

                            <div className="module-card">
                                <h3>Synchronisation</h3>
                                <p>Sync Firebase ↔ Local</p>
                                <button className="btn-primary" disabled>Bientôt disponible</button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;