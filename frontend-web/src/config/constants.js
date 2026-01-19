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
    GET_BLOCKED: `${API_BASE_URL}/users/blocked`,
    UNBLOCK: `${API_BASE_URL}/users/unblock`,
    UPDATE: `${API_BASE_URL}/users/update`,
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