import api from './api';

const chatService = {
    async getMyChats() {
        try {
            const response = await api.get('/chats');
            return { success: true, data: response.data };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to load chats' };
        }
    },

    async getChatById(chatId) {
        try {
            const response = await api.get(`/chats/${chatId}`);
            return { success: true, data: response.data };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to load chat' };
        }
    },

    async getMessages(chatId, page = 0, size = 50) {
        try {
            const response = await api.get(`/chats/${chatId}/messages`, {
                params: { page, size }
            });
            return { success: true, data: response.data };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to load messages' };
        }
    },

    async sendMessage(chatId, content) {
        try {
            const response = await api.post(`/chats/${chatId}/messages`, { content });
            return { success: true, data: response.data };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to send message' };
        }
    },

    async markAsRead(chatId) {
        try {
            await api.put(`/chats/${chatId}/read`);
            return { success: true };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to mark as read' };
        }
    },

    async getUnreadCount() {
        try {
            const response = await api.get('/chats/unread-count');
            return { success: true, data: response.data.unreadCount };
        } catch (error) {
            return { success: false, error: error.response?.data?.error || 'Failed to get unread count' };
        }
    }
};

export default chatService;
