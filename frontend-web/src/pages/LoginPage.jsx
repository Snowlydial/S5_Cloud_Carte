//?=== LOGIN PAGE COMPONENT

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Auth.css';

const LoginPage = () => {
    //*-- State for form inputs
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    //*-- Hooks from context and router
    const { login } = useAuth();
    const navigate = useNavigate();

    //*-- Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevents page reload
        setError('');
        setIsLoading(true);
        console.log("Submitting login for:", email);
        //*-- Validation
        if (!email || !password) {
            setError('Veuillez remplir tous les champs');
            setIsLoading(false);
            return;
        }

        //*-- Call login from AuthContext
        const result = await login(email, password);
        console.log("Login result:  eeee ", result);

        if (result.success ) {
            navigate('/dashboard'); // redirection fiable
        } else {
            setError(result.error || 'Échec de connexion');
        }

        setIsLoading(false);
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h1>Connexion</h1>
                <p className="auth-subtitle">Système de gestion des travaux routiers</p>

                {error && <div className="error-message">{error}</div>}

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

                    <button
                        type="submit"
                        className="btn-primary"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Connexion...' : 'Se connecter'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Pas encore de compte ? <Link to="/register">S'inscrire</Link></p>
                    <p className="guest-link">
                        <Link to="/guest-map">Consulter en tant qu'invité</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;