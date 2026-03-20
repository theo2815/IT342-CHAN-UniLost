import api from './api';

const adminService = {
  // ── Dashboard ──────────────────────────────────────────
  async getDashboardStats() {
    try {
      const response = await api.get('/admin/dashboard');
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch dashboard stats' };
    }
  },

  // ── Items ──────────────────────────────────────────────
  async getCampusItems(params = {}) {
    try {
      const response = await api.get('/admin/items', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch items' };
    }
  },

  async getFlaggedItems(params = {}) {
    try {
      const response = await api.get('/admin/items/flagged', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch flagged items' };
    }
  },

  async updateItemStatus(itemId, status) {
    try {
      const response = await api.put(`/admin/items/${itemId}/status`, { status });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to update item status' };
    }
  },

  async forceDeleteItem(itemId) {
    try {
      await api.delete(`/admin/items/${itemId}`);
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to delete item' };
    }
  },

  // ── Users ──────────────────────────────────────────────
  async getCampusUsers(params = {}) {
    try {
      const response = await api.get('/admin/users', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch users' };
    }
  },

  async updateUserStatus(userId, status) {
    try {
      const response = await api.put(`/admin/users/${userId}/status`, { status });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to update user status' };
    }
  },

  // ── Claims ─────────────────────────────────────────────
  async getCampusClaims(params = {}) {
    try {
      const response = await api.get('/admin/claims', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch claims' };
    }
  },

  // ── Analytics ──────────────────────────────────────────
  async getAnalytics() {
    try {
      const response = await api.get('/admin/analytics');
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch analytics' };
    }
  },

  // ── Cross-Campus Stats (Faculty) ──────────────────────
  async getCrossCampusStats() {
    try {
      const response = await api.get('/admin/campus-stats');
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch campus stats' };
    }
  },

  // ── Flagging (user-facing) ─────────────────────────────
  async flagItem(itemId, reason) {
    try {
      await api.post(`/items/${itemId}/flag`, { reason });
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to flag item' };
    }
  },
};

export default adminService;
