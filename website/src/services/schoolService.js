import api from './api';

const schoolService = {
    // Get all schools
    getAllSchools: async () => {
        try {
            const response = await api.get('/schools');
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch schools';
            return { success: false, error: message };
        }
    },

    // Get school by ID
    getSchoolById: async (id) => {
        try {
            const response = await api.get(`/schools/${id}`);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch school';
            return { success: false, error: message };
        }
    },

    // Create a new school
    createSchool: async (schoolData) => {
        try {
            const response = await api.post('/schools', schoolData);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to create school';
            return { success: false, error: message };
        }
    },

    // Update school
    updateSchool: async (id, schoolData) => {
        try {
            const response = await api.put(`/schools/${id}`, schoolData);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to update school';
            return { success: false, error: message };
        }
    },

    // Delete school
    deleteSchool: async (id) => {
        try {
            await api.delete(`/schools/${id}`);
            return { success: true };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to delete school';
            return { success: false, error: message };
        }
    },
};

export default schoolService;
