//?=== API ENDPOINTS & CONSTANTS

//*-- Backend API base URL (change when backend is ready)
export const API_BASE_URL = "http://localhost:8080/api";

//*-- Auth endpoints
export const AUTH_ENDPOINTS = {
    LOGIN: `${API_BASE_URL}/auth/login`,
    REGISTER: `${API_BASE_URL}/auth/register`,
    LOGOUT: `${API_BASE_URL}/auth/logout`
};

//*-- User endpoints
export const USER_ENDPOINTS = {
    LIST: `${API_BASE_URL}/users`,
    GET_BLOCKED: `${API_BASE_URL}/users/blocked`,
    UPDATE: `${API_BASE_URL}/users`,
    DELETE: `${API_BASE_URL}/users`,
    BLOCK: `${API_BASE_URL}/users/block`,
    UNBLOCK: `${API_BASE_URL}/users/unlock`,
    SYNC: `${API_BASE_URL}/users/sync`
};

//*-- Signalement (problem report) endpoints
export const SIGNALEMENT_ENDPOINTS = {
    LIST: `${API_BASE_URL}/signalements`,
    CREATE: `${API_BASE_URL}/signalements`,
    UPDATE: `${API_BASE_URL}/signalements`,
    DELETE: `${API_BASE_URL}/signalements`,
    SYNC: `${API_BASE_URL}/signalements/sync`
};

//*-- Probleme endpoints
export const PROBLEME_ENDPOINTS = {
    CREATE: `${API_BASE_URL}/problemes`,
    LIST: `${API_BASE_URL}/problemes`,
    UPDATE: `${API_BASE_URL}/problemes`,
    DELETE: `${API_BASE_URL}/problemes`,
    UPDATE_STATUS: `${API_BASE_URL}/problemes/status`
};

//*-- Entreprise endpoints
export const ENTREPRISE_ENDPOINTS = {
    LIST: `${API_BASE_URL}/entreprises`
};

//*-- Status endpoints
export const STATUS_ENDPOINTS = {
    LIST: `${API_BASE_URL}/status`
};

//*-- User roles
export const USER_ROLES = {
    VISITEUR: "VISITEUR",
    USER: "USER",
    MANAGER: "MANAGER"
};

//*-- Signalement status
export const SIGNALEMENT_STATUS = {
    NOUVEAU: "nouveau",
    EN_COURS: "en_cours",
    TERMINE: "termine"
};