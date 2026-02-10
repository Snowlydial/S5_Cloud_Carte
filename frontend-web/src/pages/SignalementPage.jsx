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
    getStatusList,
    updateProblemeStatus,
    createProbleme
} from '../services/problemeService';
import Modal from '../components/Modal';
import '../styles/SharedPages.css';

const SignalementPage = () => {
    const { hasRole } = useAuth();
    const navigate = useNavigate();

    const [signalements, setSignalements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [entreprises, setEntreprises] = useState([]);
    const [statusList, setStatusList] = useState([]);

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

    //*-- Modal state for changing status
    const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
    const [selectedProbleme, setSelectedProbleme] = useState(null);
    const [statusForm, setStatusForm] = useState({
        etat: '',
        dateStatus: ''
    });

    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    useEffect(() => {
        loadSignalements();
        loadEntreprises();
        loadStatusList();
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
        try {
            const response = await getEntreprises();
            setEntreprises(response.data || response);
        } catch (err) {
            console.error("Erreur lors du chargement des entreprises :", err);
        }
    };

    const loadStatusList = async () => {
        try {
            const response = await getStatusList();
            setStatusList(response.data || response);
        } catch (err) {
            console.error("Erreur lors du chargement des statuts :", err);
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

    //?=== STATUS MODAL HANDLERS
    const handleOpenStatusModal = (probleme) => {
        setSelectedProbleme(probleme);
        setStatusForm({
            etat: probleme.statutNom || '',
            dateStatus: new Date().toISOString().slice(0, 16)
        });
        setIsStatusModalOpen(true);
    };

    const handleCloseStatusModal = () => {
        setIsStatusModalOpen(false);
        setSelectedProbleme(null);
        setStatusForm({ etat: '', dateStatus: '' });
    };

    const handleStatusInputChange = (field, value) => {
        setStatusForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitStatus = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!statusForm.etat || !statusForm.dateStatus) {
            setError('Veuillez remplir tous les champs');
            return;
        }

        try {
            await updateProblemeStatus(selectedProbleme.idProbleme, {
                etat: statusForm.etat,
                dateStatus: statusForm.dateStatus
            });

            setSuccess('Statut mis à jour avec succès');
            handleCloseStatusModal();
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
                                                <span className={`status-badge status-${sig.problemeDTO.statutNom || 'nouveau'}`}>
                                                    {sig.problemeDTO.statutNom || 'nouveau'}
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

                                                {/* ✅ Afficher bouton ajouter problème si pas encore de problème */}
                                                {!sig.problemeDTO && (
                                                    <button
                                                        onClick={() => handleOpenModal(sig.idSignalement)}
                                                        className="btn-add-probleme"
                                                    >
                                                        Ajouter Problème
                                                    </button>
                                                )}

                                                {/* ✅ Afficher bouton changer statut si problème existe */}
                                                {sig.problemeDTO && (
                                                    <button
                                                        onClick={() => handleOpenStatusModal(sig.problemeDTO)}
                                                        className="btn-status"
                                                    >
                                                        Changer Statut
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
                        <label htmlFor="entrepriseId">Entreprise </label>
                        <select name="entrepriseId" id="entrepriseId" value={problemeForm.entrepriseId}
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

            {/* Modal for changing status */}
            <Modal
                isOpen={isStatusModalOpen}
                onClose={handleCloseStatusModal}
                title="Changer le Statut"
            >
                <form onSubmit={handleSubmitStatus} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="etat">Nouveau statut</label>
                        <select
                            id="etat"
                            value={statusForm.etat}
                            onChange={(e) => handleStatusInputChange('etat', e.target.value)}
                            required
                        >
                            <option value="">-- Sélectionner un statut --</option>
                            {statusList.map((status) => (
                                <option key={status.idStatus} value={status.nom}>
                                    {status.nom} ({status.nom === 'nouveau' ? '0%' : status.nom === 'en_cours' ? '50%' : '100%'})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="dateStatus">Date du changement</label>
                        <input
                            type="datetime-local"
                            id="dateStatus"
                            value={statusForm.dateStatus}
                            onChange={(e) => handleStatusInputChange('dateStatus', e.target.value)}
                            required
                        />
                    </div>

                    <div className="modal-actions">
                        <button type="button" onClick={handleCloseStatusModal} className="btn-cancel">
                            Annuler
                        </button>
                        <button type="submit" className="btn-submit">
                            Mettre à jour
                        </button>
                    </div>
                </form>
            </Modal>
        </div>
    );
};

export default SignalementPage;
