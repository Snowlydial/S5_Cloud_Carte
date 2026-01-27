//?=== PROBLEME SERVICE

import axios from 'axios';
import { API_BASE_URL } from '../config/constants';

//*-- API Endpoints for Probleme
export const PROBLEME_ENDPOINTS = {
    CREATE: `${API_BASE_URL}/problemes`,
    LIST: `${API_BASE_URL}/problemes`,
    UPDATE: `${API_BASE_URL}/problemes`,
    DELETE: `${API_BASE_URL}/problemes`,
    UPDATE_STATUS: `${API_BASE_URL}/problemes/status`
};

//*-- API Endpoints for related entities
export const ENTREPRISE_ENDPOINTS = {
    LIST: `${API_BASE_URL}/entreprises`
};

export const STATUS_ENDPOINTS = {
    LIST: `${API_BASE_URL}/status`
};

//*-- MOCK: Simulate data
const mockProblemes = [
    {
        id: 1,
        dateProbleme: '2026-01-15T10:30:00',
        surfaceM2: 25.5,
        budget: 500000,
        idEntreprise: 1,
        entrepriseNom: 'BuildCo',
        idCompte: 1,
        compteNom: 'Manager Test',
        idSignalement: 1,
        currentStatus: 'nouveau',
        dateStatus: '2026-01-15T10:30:00'
    },
    {
        id: 2,
        dateProbleme: '2026-01-10T14:00:00',
        surfaceM2: 40,
        budget: 800000,
        idEntreprise: 2,
        entrepriseNom: 'RoadFix Ltd',
        idCompte: 1,
        compteNom: 'Manager Test',
        idSignalement: 2,
        currentStatus: 'en_cours',
        dateStatus: '2026-01-12T09:00:00'
    }
];

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

//?=== GET ALL PROBLEMES
export const getAllProblemes = async (filters = {}) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.get(PROBLEME_ENDPOINTS.LIST, { params: filters });
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting all problemes', filters);
        let filtered = [...mockProblemes];

        if (filters.signalementId) {
            filtered = filtered.filter(p => p.idSignalement === filters.signalementId);
        }

        return await mockApiDelay(filtered);
    } catch (error) {
        console.error('Error fetching problemes:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};

//?=== CREATE PROBLEME
export const createProbleme = async (problemeData) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.post(PROBLEME_ENDPOINTS.CREATE, problemeData);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Creating probleme', problemeData);
        const newProbleme = {
            id: mockProblemes.length + 1,
            ...problemeData,
            currentStatus: 'nouveau',
            dateStatus: new Date().toISOString()
        };
        mockProblemes.push(newProbleme);
        return await mockApiDelay(newProbleme);
    } catch (error) {
        console.error('Error creating probleme:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la création');
    }
};

//?=== UPDATE PROBLEME
export const updateProbleme = async (id, data) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.put(`${PROBLEME_ENDPOINTS.UPDATE}/${id}`, data);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Updating probleme ${id}`, data);
        const index = mockProblemes.findIndex(p => p.id === id);
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
        // TODO: Uncomment when backend ready
        // const response = await axios.delete(`${PROBLEME_ENDPOINTS.DELETE}/${id}`);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Deleting probleme ${id}`);
        const index = mockProblemes.findIndex(p => p.id === id);
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
        // TODO: Uncomment when backend ready
        // const response = await axios.post(PROBLEME_ENDPOINTS.UPDATE_STATUS, {
        //   idProbleme,
        //   ...statusData
        // });
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Updating status for probleme ${idProbleme}`, statusData);
        const index = mockProblemes.findIndex(p => p.id === idProbleme);
        if (index !== -1) {
            mockProblemes[index].currentStatus = statusData.etat;
            mockProblemes[index].dateStatus = statusData.dateStatus;
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error updating status:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour du statut');
    }
};

//?=== GET ENTREPRISES LIST
export const getEntreprises = async () => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.get(ENTREPRISE_ENDPOINTS.LIST);
        // return response.data;

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
        // TODO: Uncomment when backend ready
        // const response = await axios.get(STATUS_ENDPOINTS.LIST);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting status list');
        return await mockApiDelay(mockStatus);
    } catch (error) {
        console.error('Error fetching status:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des statuts');
    }
};