import api from './api';

const campusService = {
    // Get all campuses
    getAllCampuses: async () => {
        try {
            const response = await api.get('/campuses');
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch campuses';
            return { success: false, error: message };
        }
    },

    // Get campus by ID
    getCampusById: async (id) => {
        try {
            const response = await api.get(`/campuses/${id}`);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch campus';
            return { success: false, error: message };
        }
    },

    // Get campus by email domain
    getCampusByDomain: async (domain) => {
        try {
            const response = await api.get(`/campuses/domain/${domain}`);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch campus';
            return { success: false, error: message };
        }
    },

    // Create a new campus (admin only)
    createCampus: async (campusData) => {
        try {
            const response = await api.post('/campuses', campusData);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to create campus';
            return { success: false, error: message };
        }
    },

    // Update campus (admin only)
    updateCampus: async (id, campusData) => {
        try {
            const response = await api.put(`/campuses/${id}`, campusData);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to update campus';
            return { success: false, error: message };
        }
    },

    // Delete campus (admin only)
    deleteCampus: async (id) => {
        try {
            await api.delete(`/campuses/${id}`);
            return { success: true };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to delete campus';
            return { success: false, error: message };
        }
    },
};

export default campusService;
