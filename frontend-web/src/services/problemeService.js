//?=== PROBLEME SERVICE

import axios from 'axios';
import { API_BASE_URL } from '../config/constants';

//*-- API Endpoints for Probleme
export const PROBLEME_ENDPOINTS = {
    CREATE: `${API_BASE_URL}/problemes`,
    LIST: `${API_BASE_URL}/problemes`,
    UPDATE: `${API_BASE_URL}/problemes`,
    DELETE: `${API_BASE_URL}/problemes`
};

//*-- MOCK: Simulate probleme data
const mockProblemes = [];

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
                : new Date().toISOString().split('.')[0] ,  // retire les millisecondes et Z
            surfaceM2: problemeData.surfaceM2 || 0,
            budget: problemeData.budget || 0,
            entrepriseNom: problemeData.entrepriseNom || null,
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

        return await mockApiDelay(newProbleme);

    } catch (error) {
        console.error('Error creating probleme:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la création du problème');
    }
};


//?=== GET PROBLEMES FOR SIGNALEMENT
export const getProblemesBySignalement = async (signalementId) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.get(`${PROBLEME_ENDPOINTS.LIST}?signalementId=${signalementId}`);
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