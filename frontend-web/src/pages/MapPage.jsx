//?=== MAP PAGE (Shows problems on Antananarivo map)

import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getAllSignalements } from '../services/signalementService';
import { getRecapDashboard, getStatusList } from '../services/problemeService';
import { useOfflineMap } from '../hooks/useOfflineMap';
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
    const { tileUrl, attribution, isOnline } = useOfflineMap();

    const [signalements, setSignalements] = useState([]);
    const [recapStats, setRecapStats] = useState(null);
    const [statusList, setStatusList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('');
    const [error, setError] = useState('');

    //*-- Antananarivo center coordinates (Leaflet uses [lat, lon] format)
    const center = [-18.91425, 47.52817];

    //*-- Tile bounds from mbtiles
    const tileBounds = [
        [-19.025, 47.37],   // Southwest
        [-18.772, 47.679]   // Northeast
    ];

    useEffect(() => {
        loadInitialData();
    }, []);

    useEffect(() => {
        loadSignalements();
    }, [statusFilter]);

    //*-- Load status list and initial recap data
    const loadInitialData = async () => {
        try {
            const statusData = await getStatusList();
            setStatusList(statusData);
        } catch (err) {
            console.error('Error loading initial data:', err);
        }
    };

    //*-- Load signalements based on filter
    const loadSignalements = async () => {
        setLoading(true);
        setError('');
        try {
            //*-- Build filter params for signalement API
            const filters = {};
            if (statusFilter) {
                filters.statusId = statusFilter;
            }

            const signalementsData = await getAllSignalements(filters);
            // Dans loadSignalements, après avoir reçu les data :
            console.log("filtre ",signalementsData);
            const filteredData = statusFilter
                ? signalementsData.filter(s => s.problemeDTO?.statut == statusFilter)
                : signalementsData;
            
            setSignalements(filteredData);
            // setSignalements(signalementsData.data || signalementsData);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    //*-- Load recap data separately to always show global stats
    const loadRecapData = async () => {
        try {
            const recapData = await getRecapDashboard();
            setRecapStats(recapData);
        } catch (err) {
            console.error('Error loading recap:', err);
        }
    };

    //*-- Load recap when component mounts or when signalements change
    useEffect(() => {
        if (signalements.length >= 0) {
            loadRecapData();
        }
    }, [signalements]);

    const getMarkerColor2 = (statusId) => {
        //*-- Color based on status ID (adjust according to your status IDs)
        switch (statusId) {
            case "nouveau": return '#e863f4';  // nouveau (Bleu)
            case "en_cours": return '#f39c12';  // en cours (Orange)
            case "termine": return '#27ae60';  // terminé (Vert)
            default: return '#3498db'; // Par défaut bleu
        }
    };

    const handleTileLoad = (e) => {
        console.log('✔ Tile loaded:', e.tile.src);
    };

    const handleTileError = (e) => {
        console.warn('✘ Tile failed:', e.tile.src);
    };
    const createCustomIcon = (color) => {
        return new L.DivIcon({
            html: `<svg width="25" height="41" viewBox="0 0 25 41" xmlns="http://www.w3.org/2000/svg">
                <path d="M12.5 0C5.596 0 0 5.596 0 12.5C0 21.875 12.5 41 12.5 41C12.5 41 25 21.875 25 12.5C25 5.596 19.404 0 12.5 0Z" fill="${color}" stroke="white" stroke-width="1"/>
                <circle cx="12.5" cy="12.5" r="4" fill="white" />
               </svg>`,
            className: "custom-marker-icon",
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });
    };
    const getMarkerColor = (signalement) => {
        // Si pas de problème associé = non-commencé (Bleu)
        if (!signalement.problemeDTO) return '#3498db';

        const statusId = signalement.problemeDTO?.statutNom; // Vérifiez le chemin exact de l'ID status dans votre DTO

        switch (statusId) {
            case "nouveau": return '#e863f4';  // nouveau (Bleu)
            case "en_cours": return '#f39c12';  // en cours (Orange)
            case "termine": return '#27ae60';  // terminé (Vert)
            default: return '#3498db'; // Par défaut bleu
        }
    };
    return (
        <div className="map-page">
            <div className="map-header">
                <div>
                    <h1>Carte des Travaux Routiers</h1>
                    <p>Antananarivo</p>

                    {/* Show map status indicator */}
                    <div className="map-status-indicator">
                        <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
                        <span className="status-text">
                            {isOnline ? 'En ligne (OpenStreetMap)' : 'Hors ligne (Carte locale)'}
                        </span>
                    </div>
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
                            {statusList.map(status => (
                                <option key={status.idStatus} value={status.idStatus}>
                                    {status.nom || status.etat}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Stats - Recap Dashboard */}
                    {recapStats && (
                        <div className="stats-section">
                            <h3>Récapitulatif</h3>
                            <div className="stat-item">
                                <span className="stat-label">Nombre de points</span>
                                <span className="stat-value">{recapStats.nbPoints}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Surface totale</span>
                                <span className="stat-value">{recapStats.totalSurface?.toFixed(2)} m²</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Budget total</span>
                                <span className="stat-value">{recapStats.totalBudget?.toLocaleString()} Ar</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Avancement</span>
                                <span className="stat-value">{recapStats.avancementPercent?.toFixed(1)}%</span>
                            </div>
                        </div>
                    )}

                    {/* Legend */}
                    <div className="legend-section">
                        <h3>Légende</h3>
                        {statusList.map(status => (
                            <div key={status.id} className="legend-item">
                                <span
                                    className="legend-color"
                                    style={{ backgroundColor: getMarkerColor2(status.nom) }}
                                ></span>
                                <span>{status.nom || status.etat}</span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="map-wrapper">
                    {loading ? (
                        <div className="loading-overlay">Chargement de la carte...</div>
                    ) : (
                        <MapContainer
                            center={center}
                            zoom={14}
                            style={{ height: '100%', width: '100%' }}
                            maxBounds={tileBounds}
                            maxBoundsViscosity={0.8}
                        >
                            {/* Auto-switching between online/offline tiles */}
                            <TileLayer
                                attribution={attribution}
                                url={tileUrl}
                                eventHandlers={{
                                    tileload: handleTileLoad,
                                    tileerror: handleTileError
                                }}
                                maxZoom={14}
                                minZoom={10}
                            />

                            {signalements.map((signalement) => {
                                const color = getMarkerColor(signalement);
                                const customIcon = createCustomIcon(color);

                                return (
                                    <Marker
                                        key={signalement.id}
                                        position={[signalement.latitude, signalement.longitude]}
                                        icon={customIcon} // Utilisation de l'icône colorée
                                    >
                                        <Popup>
                                            <div className="marker-popup">
                                                <h4>{signalement.description}</h4>
                                                <p><strong>Date:</strong> {new Date(signalement.dateSignalement).toLocaleDateString()}</p>
                                                <p><strong>Statut:</strong>
                                                    <span style={{ color: color, fontWeight: 'bold', marginLeft: '5px' }}>
                                                        {signalement.problemeDTO?.statutNom || "Non traité"}
                                                    </span>
                                                </p>
                                                {signalement?.problemeDTO && (
                                                    <>
                                                        <p><strong>Surface:</strong> {signalement.problemeDTO.surfaceM2} m²</p>
                                                        <p><strong>Budget:</strong> {signalement.problemeDTO.budget?.toLocaleString()} Ar</p>
                                                    </>
                                                )}
                                            </div>
                                        </Popup>
                                    </Marker>
                                );
                            })}
                        </MapContainer>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MapPage;