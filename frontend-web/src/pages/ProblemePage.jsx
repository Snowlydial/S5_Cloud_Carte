//?=== PROBLEME MANAGEMENT PAGE (Full CRUD)

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
    getAllProblemes,
    createProbleme,
    updateProbleme,
    deleteProbleme,
    updateProblemeStatus,
    getEntreprises,
    getStatusList
} from '../services/problemeService';
import { getAllSignalements } from '../services/signalementService';
import Modal from '../components/Modal';
import '../styles/Probleme.css';

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

    //*-- Create/Edit Modal
    const [isFormModalOpen, setIsFormModalOpen] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({
        dateProbleme: '',
        surfaceM2: '',
        budget: '',
        idEntreprise: '',
        idSignalement: ''
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

    //?=== CREATE/EDIT MODAL HANDLERS
    const handleOpenCreateModal = () => {
        setEditingId(null);
        setFormData({
            dateProbleme: new Date().toISOString().slice(0, 16),
            surfaceM2: '',
            budget: '',
            idEntreprise: entreprises.length > 0 ? entreprises[0].id : '',
            idSignalement: signalements.length > 0 ? signalements[0].id : ''
        });
        setIsFormModalOpen(true);
    };

    const handleOpenEditModal = (probleme) => {
        setEditingId(probleme.id);
        setFormData({
            dateProbleme: probleme.dateProbleme.slice(0, 16),
            surfaceM2: probleme.surfaceM2,
            budget: probleme.budget,
            idEntreprise: probleme.idEntreprise,
            idSignalement: probleme.idSignalement
        });
        setIsFormModalOpen(true);
    };

    const handleCloseFormModal = () => {
        setIsFormModalOpen(false);
        setEditingId(null);
        setFormData({
            dateProbleme: '',
            surfaceM2: '',
            budget: '',
            idEntreprise: '',
            idSignalement: ''
        });
    };

    const handleFormInputChange = (field, value) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitForm = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        //*-- Validation
        if (!formData.dateProbleme || !formData.surfaceM2 || !formData.budget ||
            !formData.idEntreprise || !formData.idSignalement) {
            setError('Veuillez remplir tous les champs');
            return;
        }

        try {
            const payload = {
                dateProbleme: formData.dateProbleme,
                surfaceM2: Number(formData.surfaceM2),
                budget: Number(formData.budget),
                idEntreprise: Number(formData.idEntreprise),
                idSignalement: Number(formData.idSignalement),
                idCompte: user.id || 1 // User logged ID
            };

            if (editingId) {
                await updateProbleme(editingId, payload);
                setSuccess('Problème mis à jour');
            } else {
                await createProbleme(payload);
                setSuccess('Problème créé');
            }

            handleCloseFormModal();
            loadAllData();
        } catch (err) {
            setError(err.message);
        }
    };

    //?=== STATUS MODAL HANDLERS
    const handleOpenStatusModal = (probleme) => {
        setSelectedProblemeId(probleme.id);
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
                idStatus: statusList.find(s => s.nom === statusFormData.etat)?.id || 1
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

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Gestion des Problèmes</h1>
                    <p>CRUD complet des problèmes routiers</p>
                </div>
                <div className="header-actions">
                    <button onClick={handleOpenCreateModal} className="btn-primary">
                        + Créer Problème
                    </button>
                    <button onClick={() => navigate('/signalements')} className="btn-secondary">
                        Voir signalements
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
                ) : problemes.length === 0 ? (
                    <div className="empty-state">
                        <p>Aucun problème enregistré</p>
                        <button onClick={handleOpenCreateModal} className="btn-primary">
                            Créer le premier problème
                        </button>
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
                                    <tr key={prob.id}>
                                        <td>{prob.id}</td>
                                        <td>{new Date(prob.dateProbleme).toLocaleString('fr-FR')}</td>
                                        <td>#{prob.idSignalement}</td>
                                        <td>{prob.surfaceM2}</td>
                                        <td>{prob.budget.toLocaleString()}</td>
                                        <td>{prob.entrepriseNom || prob.idEntreprise}</td>
                                        <td>
                                            <span className={`status-badge status-${prob.currentStatus}`}>
                                                {prob.currentStatus}
                                            </span>
                                        </td>
                                        <td>{new Date(prob.dateStatus).toLocaleDateString('fr-FR')}</td>
                                        <td>
                                            <div className="action-buttons">
                                                <button onClick={() => handleOpenEditModal(prob)} className="btn-edit">
                                                    Modifier
                                                </button>
                                                <button onClick={() => handleOpenStatusModal(prob)} className="btn-status">
                                                    Changer Statut
                                                </button>
                                                <button onClick={() => handleDelete(prob.id)} className="btn-delete">
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

            {/* Create/Edit Modal */}
            <Modal
                isOpen={isFormModalOpen}
                onClose={handleCloseFormModal}
                title={editingId ? 'Modifier le Problème' : 'Créer un Problème'}
            >
                <form onSubmit={handleSubmitForm} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="dateProbleme">Date du problème</label>
                        <input
                            type="datetime-local"
                            id="dateProbleme"
                            value={formData.dateProbleme}
                            onChange={(e) => handleFormInputChange('dateProbleme', e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="idSignalement">Signalement</label>
                        <select
                            id="idSignalement"
                            value={formData.idSignalement}
                            onChange={(e) => handleFormInputChange('idSignalement', e.target.value)}
                            required
                        >
                            <option value="">Sélectionner un signalement</option>
                            {signalements.map(sig => (
                                <option key={sig.id} value={sig.id}>
                                    #{sig.id} - {sig.description}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="surfaceM2">Surface (m²)</label>
                        <input
                            type="number"
                            id="surfaceM2"
                            value={formData.surfaceM2}
                            onChange={(e) => handleFormInputChange('surfaceM2', e.target.value)}
                            placeholder="Ex: 25.5"
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
                            value={formData.budget}
                            onChange={(e) => handleFormInputChange('budget', e.target.value)}
                            placeholder="Ex: 500000"
                            min="0"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="idEntreprise">Entreprise</label>
                        <select
                            id="idEntreprise"
                            value={formData.idEntreprise}
                            onChange={(e) => handleFormInputChange('idEntreprise', e.target.value)}
                            required
                        >
                            <option value="">Sélectionner une entreprise</option>
                            {entreprises.map(ent => (
                                <option key={ent.id} value={ent.id}>
                                    {ent.nom}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="modal-actions">
                        <button type="button" onClick={handleCloseFormModal} className="btn-cancel">
                            Annuler
                        </button>
                        <button type="submit" className="btn-submit">
                            {editingId ? 'Mettre à jour' : 'Créer'}
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