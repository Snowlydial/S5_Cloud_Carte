//?=== GUEST MAP PAGE (Public access - no authentication required)

import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { Link } from 'react-router-dom';
import { useOfflineMap } from '../hooks/useOfflineMap';
import axios from 'axios';
import { API_BASE_URL } from '../config/constants';
import 'leaflet/dist/leaflet.css';
import '../styles/Map.css';
import '../styles/ModalImage.css';

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

//*-- Public API endpoints (no auth required)
const PUBLIC_ENDPOINTS = {
    SIGNALEMENTS: `${API_BASE_URL}/public/signalements`,
    RECAP: `${API_BASE_URL}/public/recap`,
    STATUS: `${API_BASE_URL}/public/status`
};

const GuestMapPage = () => {
    const { tileUrl, attribution, isOnline } = useOfflineMap();

    const [signalements, setSignalements] = useState([]);
    const [recapStats, setRecapStats] = useState(null);
    const [statusList, setStatusList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('');
    const [error, setError] = useState('');

    //*-- Image modal state
    const [showImageModal, setShowImageModal] = useState(false);
    const [selectedImages, setSelectedImages] = useState([]);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    //*-- Antananarivo center coordinates
    const center = [-18.91425, 47.52817];

    //*-- Tile bounds
    const tileBounds = [
        [-19.025, 47.37],
        [-18.772, 47.679]
    ];

    useEffect(() => {
        loadInitialData();
    }, []);

    useEffect(() => {
        loadSignalements();
    }, [statusFilter]);

    const loadInitialData = async () => {
        try {
            const [statusRes, recapRes] = await Promise.all([
                axios.get(PUBLIC_ENDPOINTS.STATUS),
                axios.get(PUBLIC_ENDPOINTS.RECAP)
            ]);
            setStatusList(statusRes.data || []);
            setRecapStats(recapRes.data);
        } catch (err) {
            console.error('Error loading initial data:', err);
        }
    };

    const loadSignalements = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await axios.get(PUBLIC_ENDPOINTS.SIGNALEMENTS);
            const data = response.data || [];

            const filteredData = statusFilter
                ? data.filter(s => s.problemeDTO?.statut === statusFilter)
                : data;

            setSignalements(filteredData);
        } catch (err) {
            setError('Erreur lors du chargement des données');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getMarkerColor = (signalement) => {
        if (!signalement.problemeDTO) return '#3498db';
        const statusNom = signalement.problemeDTO?.statutNom;
        switch (statusNom) {
            case "nouveau": return '#e863f4';
            case "en_cours": return '#f39c12';
            case "termine": return '#27ae60';
            default: return '#3498db';
        }
    };

    const getMarkerColor2 = (statusNom) => {
        switch (statusNom) {
            case "nouveau": return '#e863f4';
            case "en_cours": return '#f39c12';
            case "termine": return '#27ae60';
            default: return '#3498db';
        }
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

    const handleViewPhotos = (images) => {
        if (images && images.length > 0) {
            setSelectedImages(images);
            setCurrentImageIndex(0);
            setShowImageModal(true);
        }
    };

    const closeModal = () => {
        setShowImageModal(false);
        setSelectedImages([]);
        setCurrentImageIndex(0);
    };

    const nextImage = () => {
        setCurrentImageIndex((prev) =>
            prev === selectedImages.length - 1 ? 0 : prev + 1
        );
    };

    const prevImage = () => {
        setCurrentImageIndex((prev) =>
            prev === 0 ? selectedImages.length - 1 : prev - 1
        );
    };

    return (
        <div className="map-page guest-map-page">
            <div className="map-header">
                <div>
                    <h1>Carte des Travaux Routiers</h1>
                    <p>Antananarivo - Mode Visiteur</p>
                    <div className="map-status-indicator">
                        <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
                        <span className="status-text">
                            {isOnline ? 'En ligne (OpenStreetMap)' : 'Hors ligne (Carte locale)'}
                        </span>
                    </div>
                </div>
                <div className="header-actions">
                    <Link to="/login" className="btn-primary">
                        Se connecter
                    </Link>
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
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: '#3498db' }}></span>
                            <span>Non traité</span>
                        </div>
                        {statusList.map(status => (
                            <div key={status.idStatus} className="legend-item">
                                <span
                                    className="legend-color"
                                    style={{ backgroundColor: getMarkerColor2(status.nom) }}
                                ></span>
                                <span>{status.nom}</span>
                            </div>
                        ))}
                    </div>

                    {/* Guest info */}
                    <div className="guest-info-section">
                        <p>Vous consultez en mode visiteur.</p>
                        <p><Link to="/login">Connectez-vous</Link> pour gérer les signalements.</p>
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
                            <TileLayer
                                attribution={attribution}
                                url={tileUrl}
                                maxZoom={14}
                                minZoom={10}
                            />

                            {signalements.map((signalement) => {
                                const color = getMarkerColor(signalement);
                                const customIcon = createCustomIcon(color);

                                return (
                                    <Marker
                                        key={signalement.idSignalement}
                                        position={[signalement.latitude, signalement.longitude]}
                                        icon={customIcon}
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
                                                        <p><strong>Entreprise:</strong> {signalement.problemeDTO.entrepriseNom}</p>
                                                    </>
                                                )}
                                                {signalement.lienImage && signalement.lienImage.length > 0 && (
                                                    <button
                                                        onClick={() => handleViewPhotos(signalement.lienImage)}
                                                        className="btn-view-photos"
                                                        style={{
                                                            marginTop: '10px',
                                                            padding: '5px 10px',
                                                            backgroundColor: color,
                                                            color: 'white',
                                                            border: 'none',
                                                            borderRadius: '4px',
                                                            cursor: 'pointer',
                                                            fontSize: '12px'
                                                        }}
                                                    >
                                                        📷 Voir photos ({signalement.lienImage.length})
                                                    </button>
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

            {/* Image Modal */}
            {showImageModal && (
                <div className="image-modal-overlay" onClick={closeModal}>
                    <div className="image-modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close-btn" onClick={closeModal}>×</button>
                        <div className="image-modal-body">
                            {selectedImages.length > 1 && (
                                <button className="nav-btn prev-btn" onClick={prevImage}>‹</button>
                            )}
                            <div className="image-container">
                                <img
                                    src={selectedImages[currentImageIndex]}
                                    alt={`Photo ${currentImageIndex + 1}`}
                                    className="modal-image"
                                />
                                <div className="image-counter">
                                    {currentImageIndex + 1} / {selectedImages.length}
                                </div>
                            </div>
                            {selectedImages.length > 1 && (
                                <button className="nav-btn next-btn" onClick={nextImage}>›</button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GuestMapPage;
