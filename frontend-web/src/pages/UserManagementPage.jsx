//?=== USER MANAGEMENT PAGE (Matching backend User entity)

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
    getAllUsers,
    updateUser,
    deleteUser,
    blockUser,
    unblockUser,
    syncUsers
} from '../services/userService';
import Modal from '../components/Modal';
import '../styles/SharedPages.css';

const UserManagementPage = () => {
    const { user, hasRole } = useAuth();
    const navigate = useNavigate();

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [filterBlocked, setFilterBlocked] = useState(false);

    //*-- Edit Modal (only email can be updated based on UserDTO)
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [editForm, setEditForm] = useState({
        email: ''
    });

    //*-- Fiche user
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [viewingUser, setViewingUser] = useState(null);

    const handleOpenViewModal = (user) => {
        setViewingUser(user);
        setIsViewModalOpen(true);
    };

    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setViewingUser(null);
    };

    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await getAllUsers();
            setUsers(response.data || response);
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
            const response = await syncUsers();
            setSuccess(response.data?.message || response.message || 'Synchronisation réussie');
            loadUsers();
        } catch (err) {
            setError(err.message);
        } finally {
            setSyncing(false);
        }
    };

    //?=== EDIT HANDLERS
    const handleOpenEditModal = (user) => {
        setEditingUser(user);
        setEditForm({
            email: user.email
        });
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setEditingUser(null);
        setEditForm({ email: '' });
    };

    const handleEditInputChange = (field, value) => {
        setEditForm(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmitEdit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!editForm.email) {
            setError('L\'email est requis');
            return;
        }

        try {
            await updateUser(editingUser.idUser, {
                newEmail: editForm.email
            });
            setSuccess('Utilisateur mis à jour');
            handleCloseEditModal();
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    //?=== BLOCK/UNBLOCK HANDLERS
    const handleToggleBlock = async (userId, currentlyBlocked) => {
        setError('');
        setSuccess('');

        try {
            if (currentlyBlocked) {
                await unblockUser(userId);
                setSuccess('Utilisateur débloqué (tentatives réinitialisées)');
            } else {
                await blockUser(userId);
                setSuccess('Utilisateur bloqué');
            }
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    //?=== DELETE HANDLER
    const handleDelete = async (userId) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
            return;
        }

        setError('');
        setSuccess('');
        try {
            await deleteUser(userId);
            setSuccess('Utilisateur supprimé');
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    //*-- Filter users by blocked status
    const filteredUsers = filterBlocked
        ? users.filter(u => u.isBlocked)
        : users;

    return (
        <div className="page-container">
            <div className="page-header">
                <div className="header-title">
                    <h1>Gestion des Utilisateurs</h1>
                    <p>Gérer les comptes bloqués et synchronisation</p>
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
                {/* Filter */}
                <div className="filter-bar">
                    <label className="filter-checkbox">
                        <input
                            type="checkbox"
                            checked={filterBlocked}
                            onChange={(e) => setFilterBlocked(e.target.checked)}
                        />
                        <span>Afficher uniquement les utilisateurs bloqués</span>
                    </label>
                </div>

                {loading ? (
                    <div className="loading-state">Chargement...</div>
                ) : filteredUsers.length === 0 ? (
                    <div className="empty-state">
                        <p>{filterBlocked ? 'Aucun utilisateur bloqué' : 'Aucun utilisateur'}</p>
                    </div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Email</th>
                                <th>Rôle</th>
                                <th>Tentatives</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredUsers.map((u) => (
                                <tr key={u.id}>
                                    <td>{u.id}</td>
                                    <td>{u.email}</td>
                                    <td><span className="role-badge">{u.role}</span></td>
                                    <td>{u.tentative || 0}</td>
                                    <td>
                                        <span className={`status-badge ${u.blocked ? 'status-blocked' : 'status-active'}`}>
                                            {u.blocked ? 'Bloqué' : 'Actif'}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                onClick={() => handleOpenEditModal(u)}
                                                className="btn-edit"
                                            >
                                                Modifier
                                            </button>
                                            <button
                                                onClick={() => handleOpenViewModal(u)}
                                                className="btn-view"
                                            >
                                                Voir
                                            </button>
                                            <button
                                                onClick={() => handleToggleBlock(u.email, u.blocked)}
                                                className={u.blocked ? 'btn-unblock' : 'btn-block'}
                                            >
                                                {u.blocked ? 'Débloquer' : 'Bloquer'}
                                            </button>
                                            <button
                                                onClick={() => handleDelete(u.id)}
                                                className="btn-delete"
                                            >
                                                Supprimer
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Edit Modal - only email (matching UserDTO) */}
            <Modal
                isOpen={isEditModalOpen}
                onClose={handleCloseEditModal}
                title="Modifier l'Email"
            >
                <form onSubmit={handleSubmitEdit} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            value={editForm.email}
                            onChange={(e) => handleEditInputChange('email', e.target.value)}
                            required
                        />
                    </div>

                    <p className="form-note">
                        Note: Seul l'email peut être modifié. Le rôle est géré via le profil en base de données.
                    </p>

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

            {/* View Modal - Fiche Utilisateur */}
            <Modal
                isOpen={isViewModalOpen}
                onClose={handleCloseViewModal}
                title="Fiche Utilisateur"
            >
                {viewingUser ? (
                    <div className="user-details">
                        <p><strong>ID:</strong> {viewingUser.id}</p>
                        <p><strong>Email:</strong> {viewingUser.email}</p>
                        <p><strong>Rôle:</strong> {viewingUser.role}</p>
                        <p><strong>Tentatives:</strong> {viewingUser.tentative ?? viewingUser.loginAttempts ?? 0}</p>
                        <p><strong>Statut:</strong> {(viewingUser.isBlocked || viewingUser.blocked) ? 'Bloqué' : 'Actif'}</p>
                        <p><strong>Dernière synchronisation:</strong> {viewingUser.lastSync ? new Date(viewingUser.lastSync).toLocaleString() : '-'}</p>
                    </div>
                ) : (
                    <div>Chargement...</div>
                )}

                <div className="modal-actions">
                    <button onClick={handleCloseViewModal} className="btn-cancel">Fermer</button>
                </div>
            </Modal>
        </div>
    );
};

export default UserManagementPage;
