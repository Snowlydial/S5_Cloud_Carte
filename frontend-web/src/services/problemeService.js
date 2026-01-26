//?=== PROBLEME SERVICE

import axios from 'axios';
import { API_BASE_URL } from '../config/constants';

//*-- API Endpoints for Probleme
export const PROBLEME_ENDPOINTS = {
    CREATE: `${API_BASE_URL}/problemes`,
    LIST: `${API_BASE_URL}/problemes`,
    UPDATE: `${API_BASE_URL}/problemes`,
    DELETE: `${API_BASE_URL}/problemes`,
    UPDATE_STATUS: `${API_BASE_URL}/problemes/update-status`
};

//*-- API Endpoints for related entities
export const ENTREPRISE_ENDPOINTS = {
    LIST: `${API_BASE_URL}/entreprises`
};

export const STATUS_ENDPOINTS = {
    LIST: `${API_BASE_URL}/status`
};

//*-- MOCK: Simulate probleme data
const mockProblemes = [];

//*-- MOCK: Simulate related data
const mockEntreprises = [
    { id: 1, nom: 'BuildCo' },
    { id: 2, nom: 'RoadFix Ltd' },
    { id: 3, nom: 'QuickRepair' }
];

const mockStatus = [
    { id: 1, nom: 'nouveau' },
    { id: 2, nom: 'en_cours' },
    { id: 3, nom: 'termine' }
];

const mockApiDelay = (data) => {
    return new Promise((resolve) => {
        setTimeout(() => resolve({ data }), 500);
    });
};

//?=== CREATE PROBLEME (Link to signalement)
export const createProbleme = async (signalementId, problemeData) => {
    try {
        // TODO: Décommenter quand le backend sera prêt

        //*-- MOCK version
        const token = localStorage.getItem("JWT_TOKEN");

        console.log(`[MOCK API] Creating probleme for signalement ${signalementId}`, problemeData);

        const newProbleme = {
            idProbleme: mockProblemes.length + 1,          // id auto-incrémenté
            dateProbleme: problemeData.dateProbleme
                ? problemeData.dateProbleme + "T00:00:00"   // si juste une date
                : new Date().toISOString().split('.')[0],  // retire les millisecondes et Z
            surfaceM2: problemeData.surface || 0,
            budget: problemeData.budget || 0,
            entrepriseNom: problemeData.entrepriseId || null,
            compteEmail: problemeData.compteEmail || null,
            signalementId: signalementId,
            statut: problemeData.statut || 1                 // statut par défaut
        };
        console.log('New probleme to be created:', newProbleme);
        mockProblemes.push(newProbleme);
        const response = await axios.post(PROBLEME_ENDPOINTS.CREATE, newProbleme, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;

        // return await mockApiDelay(newProbleme);

    } catch (error) {
        console.error('Error creating probleme:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la création du problème');
    }
};

//?=== GET ALL PROBLEMES (for ProblemePage full list)
export const getAllProblemes = async (filters = {}) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        const response = await axios.get(PROBLEME_ENDPOINTS.LIST, { 
            params: filters,
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;

        // //*-- MOCK version
        // console.log('[MOCK API] Getting all problemes', filters);
        // let filtered = [...mockProblemes];

        // if (filters.signalementId) {
        //     filtered = filtered.filter(p => p.signalementId === filters.signalementId);
        // }

        // return await mockApiDelay(filtered);
    } catch (error) {
        console.error('Error fetching problemes:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};

//?=== GET PROBLEMES FOR SIGNALEMENT
export const getProblemesBySignalement = async (signalementId) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.get(`${PROBLEME_ENDPOINTS.LIST}?signalementId=${signalementId}`, {
        //     headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Getting problemes for signalement ${signalementId}`);
        const filtered = mockProblemes.filter(p => p.signalementId === signalementId);
        return await mockApiDelay(filtered);
    } catch (error) {
        console.error('Error fetching problemes:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};

//?=== UPDATE PROBLEME
export const updateProbleme = async (id, data) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.put(`${PROBLEME_ENDPOINTS.UPDATE}/${id}`, data, {
        //     headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Updating probleme ${id}`, data);
        const index = mockProblemes.findIndex(p => p.idProbleme === id || p.id === id);
        if (index !== -1) {
            mockProblemes[index] = { ...mockProblemes[index], ...data };
        }
        return await mockApiDelay(mockProblemes[index]);
    } catch (error) {
        console.error('Error updating probleme:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
};

//?=== DELETE PROBLEME
export const deleteProbleme = async (id) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.delete(`${PROBLEME_ENDPOINTS.DELETE}/${id}`, {
        //     headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Deleting probleme ${id}`);
        const index = mockProblemes.findIndex(p => p.idProbleme === id || p.id === id);
        if (index !== -1) {
            mockProblemes.splice(index, 1);
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error deleting probleme:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la suppression');
    }
};

//?=== UPDATE PROBLEME STATUS
export const updateProblemeStatus = async (idProbleme, statusData) => {
    try {
        console.log("statust ",statusData)
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        const response = await axios.post(PROBLEME_ENDPOINTS.UPDATE_STATUS, {
            idProbleme,
            statusData
        }, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;

        //*-- MOCK version
        // console.log(`[MOCK API] Updating status for probleme ${idProbleme}`, statusData);
        // const index = mockProblemes.findIndex(p => p.idProbleme === idProbleme || p.id === idProbleme);
        // if (index !== -1) {
        //     mockProblemes[index].currentStatus = statusData.etat;
        //     mockProblemes[index].dateStatus = statusData.dateStatus;
        // }
        // return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error updating status:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour du statut');
    }
};

//?=== GET ENTREPRISES LIST
export const getEntreprises = async () => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        const response = await axios.get(ENTREPRISE_ENDPOINTS.LIST, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting entreprises');
        return await mockApiDelay(mockEntreprises);
    } catch (error) {
        console.error('Error fetching entreprises:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des entreprises');
    }
};

//?=== GET STATUS LIST
export const getStatusList = async () => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        const response = await axios.get(STATUS_ENDPOINTS.LIST, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting status list');
        return await mockApiDelay(mockStatus);
    } catch (error) {
        console.error('Error fetching status:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des statuts');
    }
};