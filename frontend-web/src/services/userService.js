//?=== USER MANAGEMENT SERVICE

import axios from 'axios';
import { USER_ENDPOINTS } from '../config/constants';

//*-- MOCK: Simulate user data (matching backend User entity)
const mockUsers = [
    { id: 1, email: 'manager@test.com', role: 'MANAGER', isBlocked: false, loginAttempts: 0, firebaseUid: 'mock-fb-uid-1' },
    { id: 2, email: 'user1@test.com', role: 'USER', isBlocked: true, loginAttempts: 4, firebaseUid: 'mock-fb-uid-2' },
    { id: 3, email: 'user2@test.com', role: 'USER', isBlocked: true, loginAttempts: 5, firebaseUid: 'mock-fb-uid-3' },
    { id: 4, email: 'visitor@test.com', role: 'VISITEUR', isBlocked: false, loginAttempts: 0, firebaseUid: null }
];

const mockApiDelay = (data) => {
    return new Promise((resolve) => {
        setTimeout(() => resolve({ data }), 500);
    });
};

//?=== GET ALL USERS
export const getAllUsers = async () => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        const response = await axios.get(USER_ENDPOINTS.LIST, {
          headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;

        console.log('[MOCK API] Getting all users');
        return await mockApiDelay([...mockUsers]);
    } catch (error) {
        console.error('Error fetching users:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};

//?=== GET BLOCKED USERS ONLY
export const getBlockedUsers = async () => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.get(USER_ENDPOINTS.GET_BLOCKED, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log('[MOCK API] Getting blocked users');
        const blocked = mockUsers.filter(u => u.isBlocked);
        return await mockApiDelay(blocked);
    } catch (error) {
        console.error('Error fetching blocked users:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
    }
};

//?=== UPDATE USER (email only - based on UserDTO)
export const updateUser = async (userId, userData) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.put(`${USER_ENDPOINTS.UPDATE}/${userId}`, userData, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log(`[MOCK API] Updating user ${userId}`, userData);
        const index = mockUsers.findIndex(u => u.id === userId);
        if (index !== -1) {
            mockUsers[index] = { ...mockUsers[index], ...userData };
        }
        return await mockApiDelay(mockUsers[index]);
    } catch (error) {
        console.error('Error updating user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
};

//?=== BLOCK USER
export const blockUser = async (userId) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.post(`${USER_ENDPOINTS.BLOCK}/${userId}`, {}, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log(`[MOCK API] Blocking user ${userId}`);
        const user = mockUsers.find(u => u.id === userId);
        if (user) {
            user.isBlocked = true;
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error blocking user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors du blocage');
    }
};

//?=== UNBLOCK USER (also resets login attempts)
export const unblockUser = async (userId) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.post(`${USER_ENDPOINTS.UNBLOCK}/${userId}`, {}, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log(`[MOCK API] Unblocking user ${userId}`);
        const user = mockUsers.find(u => u.id === userId);
        if (user) {
            user.isBlocked = false;
            user.loginAttempts = 0; // Reset attempts when unblocking
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error unblocking user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors du déblocage');
    }
};

//?=== DELETE USER
export const deleteUser = async (userId) => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.delete(`${USER_ENDPOINTS.DELETE}/${userId}`, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log(`[MOCK API] Deleting user ${userId}`);
        const index = mockUsers.findIndex(u => u.id === userId);
        if (index !== -1) {
            mockUsers.splice(index, 1);
        }
        return await mockApiDelay({ success: true });
    } catch (error) {
        console.error('Error deleting user:', error);
        throw new Error(error.response?.data?.message || 'Erreur lors de la suppression');
    }
};

//?=== SYNC USERS (Firebase <-> Postgres)
export const syncUsers = async () => {
    try {
        const token = localStorage.getItem("JWT_TOKEN");

        // TODO: Uncomment when backend ready
        // const response = await axios.post(USER_ENDPOINTS.SYNC, {}, {
        //   headers: { Authorization: `Bearer ${token}` }
        // });
        // return response.data;

        console.log('[MOCK API] Syncing users');
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