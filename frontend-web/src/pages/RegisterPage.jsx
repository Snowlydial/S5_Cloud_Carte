//?=== REGISTER PAGE COMPONENT

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Auth.css';

const RegisterPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [role, setRole] = useState('USER'); // nouvel état pour le rôle
    const { register } = useAuth();
    const navigate = useNavigate();

    //*-- Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setIsLoading(true);

        //*-- Validation
        if (!email || !password || !confirmPassword) {
            setError('Veuillez remplir tous les champs');
            setIsLoading(false);
            return;
        }

        if (password !== confirmPassword) {
            setError('Les mots de passe ne correspondent pas');
            setIsLoading(false);
            return;
        }

        if (password.length < 6) {
            setError('Le mot de passe doit contenir au moins 6 caractères');
            setIsLoading(false);
            return;
        }

        const roleValue = role === 'MANAGER' ? 'MANAGER' : 'USER';
        //*-- Call register from AuthContext
        const result = await register(email, password,roleValue);
        console.log("role "+roleValue)
        if (result.success) {
            setSuccess('Inscription réussie ! Redirection...');
            setTimeout(() => {
                navigate('/login');
            }, 1500);
        } else {
            setError(result.error || 'Échec d\'inscription');
        }

        setIsLoading(false);
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h1>Inscription</h1>
                <p className="auth-subtitle">Créer un compte utilisateur</p>

                {error && <div className="error-message">{error}</div>}
                {success && <div className="success-message">{success}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="exemple@email.com"
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Mot de passe</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirmer le mot de passe</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            disabled={isLoading}
                        />
                    </div>
                    {/* Sélecteur de rôle */}
                    <div className="form-group">
                        <label htmlFor="role">Rôle</label>
                        <select
                            id="role"
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                            disabled={isLoading}
                        >
                            <option value="USER">Utilisateur</option>
                            <option value="MANAGER">Manager</option>
                        </select>
                    </div>
                    <button
                        type="submit"
                        className="btn-primary"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Inscription...' : 'S\'inscrire'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Déjà un compte ? <Link to="/login">Se connecter</Link></p>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;