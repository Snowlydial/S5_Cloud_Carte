//?=== SIGNALEMENT MANAGEMENT PAGE (Manager CRUD)

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
    getAllSignalements,
    updateSignalement,
    deleteSignalement,
    syncSignalements
} from '../services/signalementService';
import { createProbleme } from '../services/problemeService';
import Modal from '../components/Modal';
import { SIGNALEMENT_STATUS } from '../config/constants';
import '../styles/Signalement.css';

const SignalementPage = () => {
    const { user, hasRole } = useAuth();
    const navigate = useNavigate();

    const [signalements, setSignalements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState({});
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    //*-- Modal state for adding probleme
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedSignalementId, setSelectedSignalementId] = useState(null);
    const [problemeForm, setProblemeForm] = useState({
        dateProbleme: '',
        surface: '',
        budget: ''
    });

    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    useEffect(() => {
        loadSignalements();
    }, []);

    const loadSignalements = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await getAllSignalements();
            setSignalements(response.data || response);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleSync = async () => {
        setSyncing(true);
        setError('');
        setSuccess('');
        try {
            const response = await syncSignalements();
            setSuccess(response.data?.message || response.message || 'Synchronisation réussie');
            loadSignalements();
        } catch (err) {
            setError(err.message);
        } finally {
            setSyncing(false);
        }
    };

    const handleEdit = (signalement) => {
        setEditingId(signalement.id);
        setEditForm({ ...signalement });
    };

    const handleCancelEdit = () => {
        setEditingId(null);
        setEditForm({});
    };

    const handleSaveEdit = async () => {
        setError('');
        setSuccess('');
        try {
            await updateSignalement(editingId, editForm);
            setSuccess('Signalement mis à jour');
            setEditingId(null);
            setEditForm({});
            loadSignalements();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce signalement ?')) {
            return;
        }

        setError('');
        setSuccess('');
        try {
            await deleteSignalement(id);
            setSuccess('Signalement supprimé');
            loadSignalements();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleInputChange = (field, value) => {
        setEditForm(prev => ({ ...prev, [field]: value }));
    };

    //?=== PROBLEME MODAL HANDLERS
    const handleOpenModal = (signalementId) => {
        setSelectedSignalementId(signalementId);
        setProblemeForm({
            dateProbleme: new Date().toISOString().split('T')[0], // Today's date
            surface: '',
            budget: ''
        });
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedSignalementId(null);
        setProblemeForm({
            dateProbleme: '',
            surface: '',
            budget: ''
        });
    };

    const handleProblemeInputChange = (field, value) => {
        setProblemeForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitProbleme = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        //*-- Validation
        if (!problemeForm.dateProbleme || !problemeForm.surface || !problemeForm.budget) {
            setError('Veuillez remplir tous les champs');
            return;
        }

        try {
            await createProbleme(selectedSignalementId, {
                dateProbleme: problemeForm.dateProbleme,
                surface: Number(problemeForm.surface),
                budget: Number(problemeForm.budget)
            });
            setSuccess('Problème ajouté avec succès');
            handleCloseModal();
            loadSignalements(); // Reload to show updated data
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Gestion des Signalements</h1>
                    <p>Gérer les informations des problèmes routiers</p>
                </div>
                <div className="header-actions">
                    <button
                        onClick={handleSync}
                        className="btn-primary"
                        disabled={syncing}
                    >
                        {syncing ? 'Synchronisation...' : 'Synchroniser'}
                    </button>
                    <button onClick={() => navigate('/map')} className="btn-secondary">
                        Voir la carte
                    </button>
                    <button onClick={() => navigate('/dashboard')} className="btn-secondary">
                        Retour
                    </button>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            <div className="content-card">
                {loading ? (
                    <div className="loading-state">Chargement...</div>
                ) : signalements.length === 0 ? (
                    <div className="empty-state">
                        <p>Aucun signalement</p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Description</th>
                                    <th>Date</th>
                                    <th>Statut</th>
                                    <th>Surface (m²)</th>
                                    <th>Budget (Ar)</th>
                                    <th>Entreprise</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {signalements.map((sig) => (
                                    <tr key={sig.id}>
                                        <td>{sig.id}</td>
                                        <td>
                                            {editingId === sig.id ? (
                                                <input
                                                    type="text"
                                                    value={editForm.description}
                                                    onChange={(e) => handleInputChange('description', e.target.value)}
                                                    className="edit-input"
                                                />
                                            ) : (
                                                sig.description
                                            )}
                                        </td>
                                        <td>{sig.date}</td>
                                        <td>
                                            {editingId === sig.id ? (
                                                <select
                                                    value={editForm.status}
                                                    onChange={(e) => handleInputChange('status', e.target.value)}
                                                    className="edit-select"
                                                >
                                                    <option value={SIGNALEMENT_STATUS.NOUVEAU}>Nouveau</option>
                                                    <option value={SIGNALEMENT_STATUS.EN_COURS}>En cours</option>
                                                    <option value={SIGNALEMENT_STATUS.TERMINE}>Terminé</option>
                                                </select>
                                            ) : (
                                                <span className={`status-badge status-${sig.status}`}>
                                                    {sig.status}
                                                </span>
                                            )}
                                        </td>
                                        <td>
                                            {editingId === sig.id ? (
                                                <input
                                                    type="number"
                                                    value={editForm.surface}
                                                    onChange={(e) => handleInputChange('surface', Number(e.target.value))}
                                                    className="edit-input"
                                                />
                                            ) : (
                                                sig.surface
                                            )}
                                        </td>
                                        <td>
                                            {editingId === sig.id ? (
                                                <input
                                                    type="number"
                                                    value={editForm.budget}
                                                    onChange={(e) => handleInputChange('budget', Number(e.target.value))}
                                                    className="edit-input"
                                                />
                                            ) : (
                                                sig.budget.toLocaleString()
                                            )}
                                        </td>
                                        <td>
                                            {editingId === sig.id ? (
                                                <input
                                                    type="text"
                                                    value={editForm.entreprise}
                                                    onChange={(e) => handleInputChange('entreprise', e.target.value)}
                                                    className="edit-input"
                                                />
                                            ) : (
                                                sig.entreprise
                                            )}
                                        </td>
                                        <td>
                                            <div className="action-buttons">
                                                {editingId === sig.id ? (
                                                    <>
                                                        <button onClick={handleSaveEdit} className="btn-save">
                                                            Sauver
                                                        </button>
                                                        <button onClick={handleCancelEdit} className="btn-cancel">
                                                            Annuler
                                                        </button>
                                                    </>
                                                ) : (
                                                    <>
                                                        <button onClick={() => handleEdit(sig)} className="btn-edit">
                                                            Modifier
                                                        </button>
                                                        <button
                                                            onClick={() => handleOpenModal(sig.id)}
                                                            className="btn-add-probleme"
                                                        >
                                                            Ajouter Problème
                                                        </button>
                                                        <button onClick={() => handleDelete(sig.id)} className="btn-delete">
                                                            Supprimer
                                                        </button>
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Modal for adding probleme */}
            <Modal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                title="Ajouter un Problème"
            >
                <form onSubmit={handleSubmitProbleme} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="dateProbleme">Date du problème</label>
                        <input
                            type="date"
                            id="dateProbleme"
                            value={problemeForm.dateProbleme}
                            onChange={(e) => handleProblemeInputChange('dateProbleme', e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="surface">Surface (m²)</label>
                        <input
                            type="number"
                            id="surface"
                            value={problemeForm.surface}
                            onChange={(e) => handleProblemeInputChange('surface', e.target.value)}
                            min="0"
                            step="0.01"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="budget">Budget (Ar)</label>
                        <input
                            type="number"
                            id="budget"
                            value={problemeForm.budget}
                            onChange={(e) => handleProblemeInputChange('budget', e.target.value)}
                            min="0"
                            required
                        />
                    </div>

                    <div className="modal-actions">
                        <button type="button" onClick={handleCloseModal} className="btn-cancel">
                            Annuler
                        </button>
                        <button type="submit" className="btn-submit">
                            Ajouter
                        </button>
                    </div>
                </form>
            </Modal>
        </div>
    );
};

export default SignalementPage;