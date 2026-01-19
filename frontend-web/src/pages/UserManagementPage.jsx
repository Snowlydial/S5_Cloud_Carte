//?=== USER MANAGEMENT PAGE (Manager only)

import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getBlockedUsers, unblockUser, syncUsers } from '../services/userService';
import '../styles/UserManagement.css';

const UserManagementPage = () => {
    const { user, hasRole } = useAuth();
    const navigate = useNavigate();

    const [blockedUsers, setBlockedUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    //*-- Redirect if not manager
    useEffect(() => {
        if (!hasRole('MANAGER')) {
            navigate('/dashboard');
        }
    }, [hasRole, navigate]);

    //*-- Load blocked users on mount
    useEffect(() => {
        loadBlockedUsers();
    }, []);

    const loadBlockedUsers = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await getBlockedUsers();
            setBlockedUsers(response.data || response);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleUnblock = async (userId) => {
        setError('');
        setSuccess('');
        try {
            await unblockUser(userId);
            setSuccess('Utilisateur débloqué avec succès');
            loadBlockedUsers(); // Reload list
        } catch (err) {
            setError(err.message);
        }
    };

    const handleSync = async () => {
        setSyncing(true);
        setError('');
        setSuccess('');
        try {
            const response = await syncUsers();
            setSuccess(response.data?.message || response.message || 'Synchronisation réussie');
            loadBlockedUsers(); // Reload list
        } catch (err) {
            setError(err.message);
        } finally {
            setSyncing(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Gestion des Utilisateurs</h1>
                    <p>Gérer les utilisateurs bloqués</p>
                </div>
                <div className="header-actions">
                    <button
                        onClick={handleSync}
                        className="btn-primary"
                        disabled={syncing}
                    >
                    {syncing ? 'Synchronisation...' : 'Synchroniser'}
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
                ) : blockedUsers.length === 0 ? (
                    <div className="empty-state">
                        <p>Aucun utilisateur bloqué</p>
                    </div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Email</th>
                                <th>Rôle</th>
                                <th>Tentatives</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {blockedUsers.map((user) => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td>{user.email}</td>
                                    <td><span className="role-badge">{user.role}</span></td>
                                    <td>{user.attempts || 0}</td>
                                    <td>
                                        <button
                                            onClick={() => handleUnblock(user.id)}
                                            className="btn-action"
                                        >
                                            Débloquer
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default UserManagementPage;