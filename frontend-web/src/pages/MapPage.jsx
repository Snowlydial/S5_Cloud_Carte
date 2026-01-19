//?=== MAP PAGE (Shows problems on Antananarivo map)

import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getAllSignalements, getRecapStats } from '../services/signalementService';
import { SIGNALEMENT_STATUS } from '../config/constants';
import 'leaflet/dist/leaflet.css';
import '../styles/Map.css';

//*-- Fix Leaflet default icon issue with React
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

const MapPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [signalements, setSignalements] = useState([]);
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('');
    const [error, setError] = useState('');

    //*-- Antananarivo center coordinates
    const center = [-18.8792, 47.5079];

    useEffect(() => {
        loadData();
    }, [statusFilter]);

    const loadData = async () => {
        setLoading(true);
        setError('');
        try {
            const filters = statusFilter ? { status: statusFilter } : {};
            const [signalementsData, statsData] = await Promise.all([
                getAllSignalements(filters),
                getRecapStats()
            ]);
            setSignalements(signalementsData.data || signalementsData);
            setStats(statsData.data || statsData);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const getMarkerColor = (status) => {
        switch (status) {
            case SIGNALEMENT_STATUS.NOUVEAU: return '#e74c3c';
            case SIGNALEMENT_STATUS.EN_COURS: return '#f39c12';
            case SIGNALEMENT_STATUS.TERMINE: return '#27ae60';
            default: return '#95a5a6';
        }
    };

    return (
        <div className="map-page">
            <div className="map-header">
                <div>
                    <h1>Carte des Travaux Routiers</h1>
                    <p>Antananarivo</p>
                </div>
                <div className="header-actions">
                    {user?.role === 'MANAGER' && (
                        <button
                            onClick={() => navigate('/signalements')}
                            className="btn-primary"
                        >
                            Gérer les signalements
                        </button>
                    )}
                    <button onClick={() => navigate('/dashboard')} className="btn-secondary">
                        Retour
                    </button>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}

            <div className="map-container-wrapper">
                <div className="map-sidebar">
                    {/* Filters */}
                    <div className="filter-section">
                        <h3>Filtres</h3>
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="filter-select"
                        >
                            <option value="">Tous les statuts</option>
                            <option value={SIGNALEMENT_STATUS.NOUVEAU}>Nouveau</option>
                            <option value={SIGNALEMENT_STATUS.EN_COURS}>En cours</option>
                            <option value={SIGNALEMENT_STATUS.TERMINE}>Terminé</option>
                        </select>
                    </div>

                    {/* Stats */}
                    {stats && (
                        <div className="stats-section">
                            <h3>Récapitulatif</h3>
                            <div className="stat-item">
                                <span className="stat-label">Nombre de points</span>
                                <span className="stat-value">{stats.total}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Surface totale</span>
                                <span className="stat-value">{stats.totalSurface} m²</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Budget total</span>
                                <span className="stat-value">{stats.totalBudget.toLocaleString()} Ar</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Avancement</span>
                                <span className="stat-value">{stats.progress}%</span>
                            </div>
                        </div>
                    )}

                    {/* Legend */}
                    <div className="legend-section">
                        <h3>Légende</h3>
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: '#e74c3c' }}></span>
                            <span>Nouveau</span>
                        </div>
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: '#f39c12' }}></span>
                            <span>En cours</span>
                        </div>
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: '#27ae60' }}></span>
                            <span>Terminé</span>
                        </div>
                    </div>
                </div>

                <div className="map-wrapper">
                    {loading ? (
                        <div className="loading-overlay">Chargement de la carte...</div>
                    ) : (
                        <MapContainer
                            center={center}
                            zoom={13}
                            style={{ height: '100%', width: '100%' }}
                        >
                            {/* TODO: Replace with offline tile server when ready */}
                            <TileLayer
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            />

                            {signalements.map((signalement) => (
                                <Marker
                                    key={signalement.id}
                                    position={[signalement.latitude, signalement.longitude]}
                                >
                                    <Popup>
                                        <div className="marker-popup">
                                            <h4>{signalement.description}</h4>
                                            <p><strong>Date:</strong> {signalement.date}</p>
                                            <p><strong>Statut:</strong> {signalement.status}</p>
                                            <p><strong>Surface:</strong> {signalement.surface} m²</p>
                                            <p><strong>Budget:</strong> {signalement.budget.toLocaleString()} Ar</p>
                                            <p><strong>Entreprise:</strong> {signalement.entreprise}</p>
                                        </div>
                                    </Popup>
                                </Marker>
                            ))}
                        </MapContainer>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MapPage;