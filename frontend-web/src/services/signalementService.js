//?=== SIGNALEMENT (PROBLEM REPORT) SERVICE

import axios from 'axios';
import { SIGNALEMENT_ENDPOINTS, SIGNALEMENT_STATUS } from '../config/constants';

//*-- MOCK: Simulate signalement data
const mockSignalements = [
    {
        id: 1,
        latitude: -18.8792,
        longitude: 47.5079,
        status: SIGNALEMENT_STATUS.NOUVEAU,
        date: '2026-01-15',
        surface: 25,
        budget: 500000,
        entreprise: 'BuildCo',
        description: 'Nid de poule profond'
    },
    {
        id: 2,
        latitude: -18.8850,
        longitude: 47.5150,
        status: SIGNALEMENT_STATUS.EN_COURS,
        date: '2026-01-10',
        surface: 40,
        budget: 800000,
        entreprise: 'RoadFix Ltd',
        description: 'Fissure importante'
    },
    {
        id: 3,
        latitude: -18.8700,
        longitude: 47.5200,
        status: SIGNALEMENT_STATUS.TERMINE,
        date: '2026-01-05',
        surface: 15,
        budget: 300000,
        entreprise: 'QuickRepair',
        description: 'Dégradation surface'
    }
];

const mockApiDelay = (data) => {
    return new Promise((resolve) => {
        setTimeout(() => resolve({ data }), 500);
    });
};
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}
export const getAllSignalements = async (filters = {}) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");
        if (!token) {
            // MOCK fallback
            console.log('[MOCK API] Getting signalements', filters);
            let filtered = [...mockSignalements];
            if (filters.status) filtered = filtered.filter(s => s.status === filters.status);
            return await mockApiDelay(filtered);
        }

        const response = await axios.get(SIGNALEMENT_ENDPOINTS.LIST, {
            params: filters,
            headers: {
                Authorization: `Bearer ${token}`, // ← ici tu envoies le token JWT
            },
        });
        console.log("Fetched signalements:", response);
        return response.data;
    } catch (error) {
        console.error('Error fetching signalements:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};


//?=== GET RECAP STATS
export const getRecapStats = async () => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.get(`${SIGNALEMENT_ENDPOINTS.LIST}/stats`);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting recap stats');
        const total = mockSignalements.length;
        const totalSurface = mockSignalements.reduce((sum, s) => sum + s.surface, 0);
        const totalBudget = mockSignalements.reduce((sum, s) => sum + s.budget, 0);
        const completed = mockSignalements.filter(s => s.status === SIGNALEMENT_STATUS.TERMINE).length;
        const progress = total > 0 ? Math.round((completed / total) * 100) : 0;

        return await mockApiDelay({
            total,
            totalSurface,
            totalBudget,
            progress
        });
    } catch (error) {
        console.error('Error fetching stats:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des stats');
    }
};

//?=== CREATE SIGNALEMENT
export const createSignalement = async (data) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.post(SIGNALEMENT_ENDPOINTS.CREATE, data);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Creating signalement', data);
        const newSignalement = {
            id: mockSignalements.length + 1,
            ...data,
            date: new Date().toISOString().split('T')[0]
        };
        mockSignalements.push(newSignalement);
        return await mockApiDelay(newSignalement);
    } catch (error) {
        console.error('Error creating signalement:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la création');
    }
};

//?=== UPDATE SIGNALEMENT
export const updateSignalement = async (id, data) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.put(`${SIGNALEMENT_ENDPOINTS.UPDATE}/${id}`, data);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Updating signalement ${id}`, data);
        const index = mockSignalements.findIndex(s => s.id === id);
        if (index !== -1) {
            mockSignalements[index] = { ...mockSignalements[index], ...data };
        }
        return await mockApiDelay(mockSignalements[index]);
    } catch (error) {
        console.error('Error updating signalement:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
};

//?=== DELETE SIGNALEMENT
export const deleteSignalement = async (id) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.delete(`${SIGNALEMENT_ENDPOINTS.DELETE}/${id}`);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Deleting signalement ${id}`);
        const index = mockSignalements.findIndex(s => s.id === id);
        if (index !== -1) {
            mockSignalements.splice(index, 1);
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error deleting signalement:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la suppression');
    }
};

//?=== SYNC SIGNALEMENTS (Firebase <-> Postgres)
export const syncSignalements = async () => {
    try {
        // TODO: Uncomment when backend ready
        const token = localStorage.getItem("JWT_TOKEN");

        const response = await axios.get(SIGNALEMENT_ENDPOINTS.SYNC, {
            headers: {
                Authorization: `Bearer ${token}`, 
            },
        });
        return response.data;


    } catch (error) {
        console.error('Error syncing signalements:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la synchronisation');
    }
};