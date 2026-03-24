import api from './api';

const userService = {
    async getUserById(userId) {
        try {
            const response = await api.get(`/users/${userId}`);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch user' };
        }
    },

    async updateProfile(userId, data) {
        try {
            const response = await api.put(`/users/${userId}`, data);
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to update profile' };
        }
    },

    async uploadProfilePicture(userId, file) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            const response = await api.post(`/users/${userId}/profile-picture`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to upload profile picture' };
        }
    },

    async changePassword(userId, { currentPassword, newPassword }) {
        try {
            const response = await api.put(`/users/${userId}/change-password`, {
                currentPassword,
                newPassword,
            });
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to change password' };
        }
    },

    async getLeaderboard(size = 20, campusId) {
        try {
            const params = { size };
            if (campusId) params.campusId = campusId;
            const response = await api.get('/users/leaderboard', { params });
            return { success: true, data: response.data };
        } catch (err) {
            return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch leaderboard' };
        }
    },
};

export default userService;
