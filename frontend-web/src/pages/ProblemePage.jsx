//?=== PROBLEME MANAGEMENT PAGE (Full CRUD)

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
    getAllProblemes,
    updateProbleme,
    deleteProbleme,
    updateProblemeStatus,
    getEntreprises,
    getStatusList,
    asyncProblemes
} from '../services/problemeService';
import { getAllSignalements } from '../services/signalementService';
import Modal from '../components/Modal';
import '../styles/SharedPages.css';

const ProblemePage = () => {
    const { user, hasRole } = useAuth();
    const navigate = useNavigate();

    const [problemes, setProblemes] = useState([]);
    const [signalements, setSignalements] = useState([]);
    const [entreprises, setEntreprises] = useState([]);
    const [statusList, setStatusList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [syncing, setSyncing] = useState(false);

    //*-- Edit Modal
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState({
        dateProbleme: '',
        surfaceM2: '',
        budget: '',
        entrepriseNom: ''
    });

    //*-- Status Modal
    const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);
    const [selectedProblemeId, setSelectedProblemeId] = useState(null);
    const [statusFormData, setStatusFormData] = useState({
        etat: '',
        dateStatus: ''
    });

    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    useEffect(() => {
        loadAllData();
    }, []);

    const loadAllData = async () => {
        setLoading(true);
        setError('');
        try {
            const [problemesRes, signalementsRes, entreprisesRes, statusRes] = await Promise.all([
                getAllProblemes(),
                getAllSignalements(),
                getEntreprises(),
                getStatusList()
            ]);

            setProblemes(problemesRes.data || problemesRes);
            setSignalements(signalementsRes.data || signalementsRes);
            setEntreprises(entreprisesRes.data || entreprisesRes);
            setStatusList(statusRes.data || statusRes);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    //?=== EDIT MODAL HANDLERS
    const handleOpenEditModal = (probleme) => {
        setEditingId(probleme.idProbleme || probleme.id);
        setEditForm({
            dateProbleme: (probleme.dateProbleme || '').slice(0, 16),
            surfaceM2: probleme.surfaceM2,
            budget: probleme.budget,
            entrepriseNom: probleme.entrepriseNom || ''
        });
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setEditingId(null);
        setEditForm({
            dateProbleme: '',
            surfaceM2: '',
            budget: '',
            entrepriseNom: ''
        });
    };

    const handleEditInputChange = (field, value) => {
        setEditForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitEdit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!editForm.dateProbleme || !editForm.surfaceM2 || !editForm.budget) {
            setError('Veuillez remplir tous les champs obligatoires');
            return;
        }

        try {
            const payload = {
                dateProbleme: editForm.dateProbleme,
                surfaceM2: Number(editForm.surfaceM2),
                budget: Number(editForm.budget),
                entrepriseNom: editForm.entrepriseNom
            };

            await updateProbleme(editingId, payload);
            setSuccess('Problème mis à jour');
            handleCloseEditModal();
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };

    //?=== STATUS MODAL HANDLERS
    const handleOpenStatusModal = (probleme) => {
        setSelectedProblemeId(probleme.idProbleme || probleme.id);
        setStatusFormData({
            etat: probleme.currentStatus || (statusList.length > 0 ? statusList[0].nom : ''),
            dateStatus: new Date().toISOString().slice(0, 16)
        });
        setIsStatusModalOpen(true);
    };

    const handleCloseStatusModal = () => {
        setIsStatusModalOpen(false);
        setSelectedProblemeId(null);
        setStatusFormData({ etat: '', dateStatus: '' });
    };

    const handleStatusInputChange = (field, value) => {
        setStatusFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitStatus = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!statusFormData.etat || !statusFormData.dateStatus) {
            setError('Veuillez remplir tous les champs');
            return;
        }

        try {
            await updateProblemeStatus(selectedProblemeId, {
                etat: statusFormData.etat,
                dateStatus: statusFormData.dateStatus,
                statutId: statusList.find(s => s.nom === statusFormData.etat)?.idStatus || 1
            });

            setSuccess('Statut mis à jour');
            handleCloseStatusModal();
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };

    //?=== DELETE HANDLER
    const handleDelete = async (id) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce problème ?')) {
            return;
        }

        setError('');
        setSuccess('');
        try {
            await deleteProbleme(id);
            setSuccess('Problème supprimé');
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };
    
    const handleSync = async () => {
        setSyncing(true);
        setError('');
        setSuccess('');
        try {
            const response = await asyncProblemes();
            setSuccess(response.data?.message || response.message || 'Synchronisation réussie');
            loadAllData();
        } catch (err) {
            setError(err.message);
        } finally {
            setSyncing(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div className="header-title">
                    <h1>Gestion des Problèmes</h1>
                    <p>CRUD complet des problèmes avec statuts et entreprises</p>
                </div>
                <div className="header-actions">
                    <button
                        onClick={handleSync}
                        className="btn-primary"
                        disabled={syncing}
                    >
                        {syncing ? 'SYNC...' : 'SYNCHRONISER'}
                    </button>
                    <button onClick={() => navigate('/signalements')} className="btn-secondary">
                        Voir signalements
                    </button>
                </div>
            </div>

            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}

            <div className="content-card">
                {loading ? (
                    <div className="loading-state">Chargement...</div>
                ) : problemes.length === 0 ? (
                    <div className="empty-state">
                        <p>Aucun problème enregistré</p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Date</th>
                                    <th>Signalement</th>
                                    <th>Surface (m²)</th>
                                    <th>Budget (Ar)</th>
                                    <th>Entreprise</th>
                                    <th>Statut</th>
                                    <th>Date Statut</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {problemes.map((prob) => (
                                    <tr key={prob.idProbleme || prob.id}>
                                        <td>{prob.idProbleme || prob.id}</td>
                                        <td>{new Date(prob.dateProbleme).toLocaleString('fr-FR')}</td>
                                        <td>#{prob.signalementId || prob.idSignalement}</td>
                                        <td>{prob.surfaceM2}</td>
                                        <td>{Number(prob.budget).toLocaleString()}</td>
                                        <td>{prob.entrepriseNom || '-'}</td>
                                        <td>
                                            <span className={`status-badge status-${prob.currentStatus || prob.statut}`}>
                                                {prob.currentStatus || prob.statut || 'nouveau'}
                                            </span>
                                        </td>
                                        <td>{prob.dateProbleme ? new Date(prob.dateProbleme).toLocaleDateString('fr-FR') : '-'}</td>
                                        <td>
                                            <div className="action-buttons">
                                                <button onClick={() => handleOpenEditModal(prob)} className="btn-edit">
                                                    Modifier
                                                </button>
                                                <button onClick={() => handleOpenStatusModal(prob)} className="btn-status">
                                                    Changer Statut
                                                </button>
                                                <button onClick={() => handleDelete(prob.idProbleme || prob.id)} className="btn-delete">
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

            {/* Edit Modal */}
            <Modal
                isOpen={isEditModalOpen}
                onClose={handleCloseEditModal}
                title="Modifier le Problème"
            >
                <form onSubmit={handleSubmitEdit} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="dateProbleme">Date du problème</label>
                        <input
                            type="datetime-local"
                            id="dateProbleme"
                            value={editForm.dateProbleme}
                            onChange={(e) => handleEditInputChange('dateProbleme', e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="surfaceM2">Surface (m²)</label>
                        <input
                            type="number"
                            id="surfaceM2"
                            value={editForm.surfaceM2}
                            onChange={(e) => handleEditInputChange('surfaceM2', e.target.value)}
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
                            value={editForm.budget}
                            onChange={(e) => handleEditInputChange('budget', e.target.value)}
                            min="0"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="entrepriseNom">Entreprise</label>
                        <select name="entrepriseNom" id="entrepriseNom" value={editForm.entrepriseNom}
                            onChange={(e) => handleEditInputChange('entrepriseNom', e.target.value)}>
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
                        <button type="button" onClick={handleCloseEditModal} className="btn-cancel">
                            Annuler
                        </button>
                        <button type="submit" className="btn-submit">
                            Mettre à jour
                        </button>
                    </div>
                </form>
            </Modal>

            {/* Status Modal */}
            <Modal
                isOpen={isStatusModalOpen}
                onClose={handleCloseStatusModal}
                title="Modifier le Statut"
            >
                <form onSubmit={handleSubmitStatus} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="etat">Nouveau statut</label>
                        <select
                            id="etat"
                            value={statusFormData.etat}
                            onChange={(e) => handleStatusInputChange('etat', e.target.value)}
                            required
                        >
                            <option value="">Sélectionner un statut</option>
                            {statusList.map(status => (
                                <option key={status.id} value={status.nom}>
                                    {status.nom}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="dateStatus">Date du changement</label>
                        <input
                            type="datetime-local"
                            id="dateStatus"
                            value={statusFormData.dateStatus}
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

export default ProblemePage;
