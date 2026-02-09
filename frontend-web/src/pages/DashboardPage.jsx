//?=== DASHBOARD PAGE (Reserved for future stats)

import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Dashboard.css';

const DashboardPage = () => {
    const { user } = useAuth();

    return (
        <div className="dashboard-wrapper">
            <div className="dashboard-hero">
                <div className="hero-content">
                    <h1 className="hero-title">ROADFIX ANTANANARIVO</h1>
                    <p className="hero-subtitle">Système de gestion des travaux routiers</p>
                </div>
                <div className="hero-badge-group">
                    <div className="hero-badge">
                        <span className="badge-label">USER</span>
                        <span className="badge-value">{user?.email}</span>
                    </div>
                    <div className="hero-badge role-badge-special">
                        <span className="badge-value">{user?.role}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;
