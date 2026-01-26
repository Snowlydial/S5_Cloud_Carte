//?=== DASHBOARD PAGE (Main page after login)

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

                <div className="modules-grid">
                    <div className="module-card">
                        <h3>Carte & Signalements</h3>
                        <p>Visualiser les problèmes routiers sur la carte d'Antananarivo</p>
                        <button
                            onClick={() => navigate('/map')}
                            className="btn-primary"
                        >
                            Voir la carte
                        </button>
                    </div>

                    {user?.role === 'MANAGER' && (
                        <>
                            <div className="module-card">
                                <h3>Gestion Utilisateurs</h3>
                                <p>Gérer les comptes bloqués et synchroniser les utilisateurs</p>
                                <button
                                    onClick={() => navigate('/users')}
                                    className="btn-primary"
                                >
                                    Gérer les utilisateurs
                                </button>
                            </div>

                            <div className="module-card">
                                <h3>Gestion Signalements</h3>
                                <p>CRUD des signalements et synchronisation Firebase</p>
                                <button
                                    onClick={() => navigate('/signalements')}
                                    className="btn-primary"
                                >
                                    Gérer les signalements
                                </button>
                            </div>

                            <div className="module-card">
                                <h3>Gestion Problèmes</h3>
                                <p>CRUD complet des problèmes avec statuts et entreprises</p>
                                <button
                                    onClick={() => navigate('/problemes')}
                                    className="btn-primary"
                                >
                                    Gérer les problèmes
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;