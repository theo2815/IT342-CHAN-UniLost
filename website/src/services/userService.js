import api from './api';

const userService = {
    async updateProfile(userId, data) {
        try {
            const response = await api.put(`/users/${userId}`, data);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to update profile' };
        }
    },
};

export default userService;
