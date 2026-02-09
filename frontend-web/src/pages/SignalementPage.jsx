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
import {
    getEntreprises,
} from '../services/problemeService';
import { createProbleme } from '../services/problemeService';
import Modal from '../components/Modal';
import { SIGNALEMENT_STATUS } from '../config/constants';
import '../styles/SharedPages.css';

const SignalementPage = () => {
    const { user, hasRole } = useAuth();
    const navigate = useNavigate();

    const [signalements, setSignalements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [entreprises, setEntreprises] = useState([]);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    //*-- Modal state for editing signalement
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState({});

    //*-- Modal state for adding probleme
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedSignalementId, setSelectedSignalementId] = useState(null);
    const [problemeForm, setProblemeForm] = useState({
        dateProbleme: '',
        surface: '',
        budget: '',
        entrepriseId: ''
    });

    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    useEffect(() => {
        loadSignalements();
        loadEntreprises();
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
    const loadEntreprises = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await getEntreprises(); // on attend la promesse
            setEntreprises(response.data || response);
            console.log("Entreprises :", response);
        } catch (err) {
            console.error("Erreur lors du chargement des entreprises :", err);
            setError(err.message || "Erreur lors du chargement");
        } finally {
            setLoading(false);
        }
    };

    const handleEditInputChange = (field, value) => {
        setEditForm(prev => ({ ...prev, [field]: value }));
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

    //?=== EDIT MODAL HANDLERS
    const handleOpenEditModal = (signalement) => {
        setEditingId(signalement.idSignalement);
        setEditForm({ ...signalement });
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setEditingId(null);
        setEditForm({});
    };

    const handleSaveEdit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        try {
            await updateSignalement(editingId, editForm);
            setSuccess('Signalement mis à jour');
            setIsEditModalOpen(false);
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

    //?=== PROBLEME MODAL HANDLERS
    const handleOpenModal = (signalementId) => {
        setSelectedSignalementId(signalementId);
        setProblemeForm({
            dateProbleme: new Date().toISOString().split('T')[0], // Today's date
            surface: '',
            budget: '',
            entrepriseId: ''
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
                budget: Number(problemeForm.budget),
                entrepriseId: Number(problemeForm.entrepriseId)
            });

            setSuccess('Problème ajouté avec succès');
            handleCloseModal();
            loadSignalements();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div className="header-title">
                    <h1>Gestion des Signalements</h1>
                    <p>CRUD des signalements et synchronisation Firebase</p>
                </div>
                <div className="header-actions">
                    <button
                        onClick={handleSync}
                        className="btn-primary"
                        disabled={syncing}
                    >
                        {syncing ? 'SYNC...' : 'SYNCHRONISER'}
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
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {signalements.map((sig) => (
                                    <tr key={sig.idSignalement}>
                                        <td>{sig.idSignalement}</td>
                                        <td>{sig.description}</td>
                                        <td>{new Date(sig.dateSignalement).toLocaleDateString()}</td>
                                        <td>
                                            {/* ✅ Afficher le statut en fonction de l'existence du problème */}
                                            {sig.problemeDTO ? (
                                                <span className="status-badge status-traite">
                                                    Déjà traité
                                                </span>
                                            ) : (
                                                <span className="status-badge status-non-traite">
                                                    Non traité
                                                </span>
                                            )}
                                        </td>
                                        <td>{sig.problemeDTO?.surfaceM2}</td>
                                        <td>
                                            <div className="action-buttons">
                                                <button onClick={() => handleOpenEditModal(sig)} className="btn-edit">
                                                    Modifier
                                                </button>

                                                {/* ✅ Afficher le bouton seulement si pas encore de problème */}
                                                {!sig.problemeDTO && (
                                                    <button
                                                        onClick={() => handleOpenModal(sig.idSignalement)}
                                                        className="btn-add-probleme"
                                                    >
                                                        Ajouter Problème
                                                    </button>
                                                )}

                                                <button onClick={() => handleDelete(sig.idSignalement)} className="btn-delete">
                                                    Supprimer
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Modal for editing signalement */}
            <Modal
                isOpen={isEditModalOpen}
                onClose={handleCloseEditModal}
                title="Modifier le Signalement"
            >
                <form onSubmit={handleSaveEdit} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <input
                            type="text"
                            id="description"
                            value={editForm.description || ''}
                            onChange={(e) => handleEditInputChange('description', e.target.value)}
                            required
                        />
                    </div>
                    <div className="modal-actions">
                        <button type="button" onClick={handleCloseEditModal} className="btn-cancel">
                            Annuler
                        </button>
                        <button type="submit" className="btn-submit">
                            Mettre à jour
                        </button>
                    </div>
                </form>
            </Modal>

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
                    <div className="form-group">
                        <label htmlFor="budget">Entreprise </label>
                        <select name="entrepriseId" id="entrepriseId" value={editForm.entrepriseNom}
                            onChange={(e) => handleProblemeInputChange('entrepriseId', e.target.value)}>
                            <option value="">-- Sélectionner une entreprise --</option>

                            {entreprises.map((entreprise) => (
                                <option
                                    key={entreprise.idEntreprise}
                                    value={entreprise.idEntreprise}
                                >
                                    {entreprise.nom}
                                </option>
                            ))}
                        </select>
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
