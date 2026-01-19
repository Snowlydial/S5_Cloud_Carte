//?=== USER MANAGEMENT SERVICE

import axios from 'axios';
import { USER_ENDPOINTS } from '../config/constants';

//*-- MOCK: Simulate backend user data
const mockUsers = [
    { id: 1, email: 'user1@test.com', role: 'USER', blocked: true, attempts: 3 },
    { id: 2, email: 'user2@test.com', role: 'USER', blocked: true, attempts: 4 },
    { id: 3, email: 'user3@test.com', role: 'USER', blocked: false, attempts: 0 }
];

const mockApiDelay = (data) => {
    return new Promise((resolve) => {
        setTimeout(() => resolve({ data }), 500);
    });
};

//?=== GET BLOCKED USERS
export const getBlockedUsers = async () => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.get(USER_ENDPOINTS.GET_BLOCKED);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Getting blocked users');
        const blocked = mockUsers.filter(u => u.blocked);
        return await mockApiDelay(blocked);
    } catch (error) {
        console.error('Error fetching blocked users:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des utilisateurs bloqués');
    }
};

//?=== UNBLOCK USER
export const unblockUser = async (userId) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.post(`${USER_ENDPOINTS.UNBLOCK}/${userId}`);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Unblocking user ${userId}`);
        const user = mockUsers.find(u => u.id === userId);
        if (user) {
            user.blocked = false;
            user.attempts = 0;
        }
        return await mockApiDelay({ success: true, message: 'Utilisateur débloqué' });
    } catch (error) {
        console.error('Error unblocking user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors du déblocage');
    }
};

//?=== UPDATE USER INFO
export const updateUser = async (userId, userData) => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.put(`${USER_ENDPOINTS.UPDATE}/${userId}`, userData);
        // return response.data;

        //*-- MOCK version
        console.log(`[MOCK API] Updating user ${userId}`, userData);
        return await mockApiDelay({ success: true, user: { ...userData, id: userId } });
    } catch (error) {
        console.error('Error updating user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
};

//?=== SYNC USERS (Firebase <-> Postgres)
export const syncUsers = async () => {
    try {
        // TODO: Uncomment when backend ready
        // const response = await axios.post(USER_ENDPOINTS.SYNC);
        // return response.data;

        //*-- MOCK version
        console.log('[MOCK API] Syncing users between Firebase and Postgres');
        return await mockApiDelay({
            success: true,
            synced: 5,
            message: 'Synchronisation réussie: 5 utilisateurs'
        });
    } catch (error) {
        console.error('Error syncing users:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la synchronisation');
    }
};