import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Layout.css';

const Sidebar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    //?=== Navigation items based on user role
    const getNavItems = () => {
        const baseItems = [
            { path: '/dashboard', label: 'Dashboard', icon: '■' },
            { path: '/map', label: 'Carte', icon: '▲' }
        ];

        //*-- Manager specific routes
        if (user?.role === 'MANAGER') {
            return [
                ...baseItems,
                { path: '/problemes', label: 'Problemes', icon: '●' },
                { path: '/signalements', label: 'Signalement', icon: '◆' },
                { path: '/users', label: 'Utilisateurs', icon: '▼' }
            ];
        }

        return baseItems;
    };

    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <h1 className="sidebar-logo">ROADFIX</h1>
                <div className="sidebar-badge">{user?.role || 'USER'}</div>
            </div>

            <nav className="sidebar-nav">
                {getNavItems().map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) =>
                            `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`
                        }
                    >
                        <span className="sidebar-icon">{item.icon}</span>
                        <span className="sidebar-label">{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="sidebar-user">
                    <div className="sidebar-user-avatar">
                        {user?.email?.[0]?.toUpperCase() || 'U'}
                    </div>
                    <div className="sidebar-user-info">
                        <div className="sidebar-user-email">{user?.email || 'User'}</div>
                        <div className="sidebar-user-role">{user?.role || 'VISITOR'}</div>
                    </div>
                </div>
                <button onClick={handleLogout} className="sidebar-logout">
                    LOGOUT
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
