import api from '../../shared/services/api';

const notificationService = {
    getNotifications: async (page = 0, size = 20) => {
        try {
            const response = await api.get('/notifications', { params: { page, size } });
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to fetch notifications';
            return { success: false, error: message };
        }
    },

    getUnreadCount: async () => {
        try {
            const response = await api.get('/notifications/unread/count');
            return { success: true, data: response.data.count };
        } catch (error) {
            return { success: false, error: error.message, data: 0 };
        }
    },

    markAsRead: async (id) => {
        try {
            const response = await api.put(`/notifications/${id}/read`);
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to mark as read';
            return { success: false, error: message };
        }
    },

    markAllAsRead: async () => {
        try {
            await api.put('/notifications/read-all');
            return { success: true };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to mark all as read';
            return { success: false, error: message };
        }
    },

    deleteNotification: async (id) => {
        try {
            await api.delete(`/notifications/${id}`);
            return { success: true };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to delete notification';
            return { success: false, error: message };
        }
    },
};

export default notificationService;
