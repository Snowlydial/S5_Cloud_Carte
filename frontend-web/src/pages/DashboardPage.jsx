//?=== DASHBOARD PAGE (Stats and overview)

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRecapDashboard } from '../services/problemeService';
import '../styles/Dashboard.css';

const DashboardPage = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        setLoading(true);
        setError('');
        try {
            const data = await getRecapDashboard();
            setStats(data);
        } catch (err) {
            setError('Erreur lors du chargement des statistiques');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="dashboard-wrapper">
            <div className="dashboard-hero">
                <div className="hero-content">
                    <h1 className="hero-title">ROADFIX ANTANANARIVO</h1>
                    <p className="hero-subtitle">Système de gestion des travaux routiers</p>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}

            {loading ? (
                <div className="loading-state">Chargement des statistiques...</div>
            ) : stats && (
                <div className="dashboard-content">
                    {/* Recap Cards */}
                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-icon">📍</div>
                            <div className="stat-info">
                                <span className="stat-value">{stats.nbSignalements || 0}</span>
                                <span className="stat-label">Signalements</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <div className="stat-icon">📐</div>
                            <div className="stat-info">
                                <span className="stat-value">{stats.totalSurface?.toFixed(2)} m²</span>
                                <span className="stat-label">Surface totale</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <div className="stat-icon">💰</div>
                            <div className="stat-info">
                                <span className="stat-value">{stats.totalBudget?.toLocaleString()} Ar</span>
                                <span className="stat-label">Budget total</span>
                            </div>
                        </div>

                        <div className="stat-card highlight">
                            <div className="stat-icon">📊</div>
                            <div className="stat-info">
                                <span className="stat-value">{stats.avancementPercent?.toFixed(1)}%</span>
                                <span className="stat-label">Avancement global</span>
                            </div>
                        </div>
                    </div>

                    {/* Statistics Table */}
                    <div className="stats-table-section">
                        <h2>Tableau de Statistiques</h2>
                        <div className="stats-table-container">
                            <table className="stats-table">
                                <thead>
                                    <tr>
                                        <th>Statut</th>
                                        <th>Nombre</th>
                                        <th>Avancement</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Nouveau</td>
                                        <td className="value-cell">{stats.nbNouveaux || 0}</td>
                                        <td>0% d'avancement</td>
                                    </tr>
                                    <tr>
                                        <td>En Cours</td>
                                        <td className="value-cell">{stats.nbEnCours || 0}</td>
                                        <td>50% d'avancement</td>
                                    </tr>
                                    <tr>
                                        <td>Terminé</td>
                                        <td className="value-cell">{stats.nbTermines || 0}</td>
                                        <td>100% d'avancement</td>
                                    </tr>
                                    <tr className="highlight-row">
                                        <td>Délai Moyen</td>
                                        <td className="value-cell">{stats.delaiMoyenJours?.toFixed(1) || 0} jours</td>
                                        <td>Du problème créé à terminé</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {/* Quick Actions */}
                    <div className="quick-actions">
                        <h2>Actions Rapides</h2>
                        <div className="actions-grid">
                            <button onClick={() => navigate('/map')} className="action-btn map-btn">
                                🗺️ Voir la Carte
                            </button>
                            <button onClick={() => navigate('/signalements')} className="action-btn signalement-btn">
                                📋 Gérer Signalements
                            </button>
                            <button onClick={() => navigate('/problemes')} className="action-btn probleme-btn">
                                🔧 Gérer Problèmes
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DashboardPage;
