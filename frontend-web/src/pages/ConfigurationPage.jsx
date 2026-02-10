//?=== CONFIGURATION PAGE

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/Dashboard.css';

const ConfigurationPage = () => {
    const [config, setConfig] = useState({
        m2_forfaitaire: '',
        tentative_max: ''
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const token = localStorage.getItem("JWT_TOKEN"); // JWT pour auth si nécessaire

    // Charger la configuration depuis le backend
    useEffect(() => {
        const loadConfig = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await axios.get('http://localhost:8080/api/configuration', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                setConfig({
                    m2_forfaitaire: response.data.m2_forfaitaire,
                    tentative_max: response.data.tentative_max
                });
            } catch (err) {
                setError('Erreur lors du chargement de la configuration');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        loadConfig();
    }, []);

    // Gestion des changements de champ
    const handleInputChange = (field, value) => {
        setConfig(prev => ({ ...prev, [field]: value }));
    };

    // Sauvegarder la configuration
    const handleSaveConfig = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await axios.put('http://localhost:8080/api/configuration', config, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setSuccess('Configuration mise à jour avec succès !');
        } catch (err) {
            setError('Erreur lors de la sauvegarde de la configuration');
            console.error(err);
        }
    };

    return (
        <div className="dashboard-wrapper">
            <div className="dashboard-hero">
                <div className="hero-content">
                    <h1 className="hero-title">CONFIGURATION</h1>
                    <p className="hero-subtitle">Modifier le prix au m² et le nombre de tentatives</p>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            {loading ? (
                <div className="loading-state">Chargement de la configuration...</div>
            ) : (
                <div className="dashboard-content">
                    <form onSubmit={handleSaveConfig} className="modal-form">
                        <div className="form-group">
                            <label htmlFor="m2_forfaitaire">Prix au m² (Ar)</label>
                            <input
                                type="number"
                                id="m2_forfaitaire"
                                value={config.m2_forfaitaire}
                                onChange={(e) => handleInputChange('m2_forfaitaire', e.target.value)}
                                min="0"
                                step="0.01"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="tentative_max">Nombre de tentatives max</label>
                            <input
                                type="number"
                                id="tentative_max"
                                value={config.tentative_max}
                                onChange={(e) => handleInputChange('tentative_max', e.target.value)}
                                min="1"
                                required
                            />
                        </div>

                        <div className="modal-actions">
                            <button type="submit" className="btn-submit">
                                Sauvegarder
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
};

export default ConfigurationPage;
