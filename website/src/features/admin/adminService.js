import api from '../../shared/services/api';

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

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

  // ── Item Trends ──────────────────────────────────────
  async getItemTrends(months = 12) {
    try {
      const response = await api.get('/admin/item-trends', { params: { months } });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch item trends' };
    }
  },

  // ── Cross-Campus Stats ───────────────────────────────
  async getCrossCampusStats() {
    try {
      const response = await api.get('/admin/campus-stats');
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch campus stats' };
    }
  },

  // ── System Health ────────────────────────────────────
  async getSystemHealth() {
    try {
      const response = await api.get('/admin/health');
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch health data' };
    }
  },

  // ── Bulk Actions ─────────────────────────────────────
  async bulkUpdateItemStatus(ids, status) {
    try {
      const response = await api.put('/admin/items/bulk-status', { ids, status });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Bulk update failed' };
    }
  },

  async bulkDeleteItems(ids) {
    try {
      const response = await api.delete('/admin/items/bulk-delete', { data: { ids } });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Bulk delete failed' };
    }
  },

  async bulkUpdateUserStatus(ids, status) {
    try {
      const response = await api.put('/admin/users/bulk-status', { ids, status });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Bulk update failed' };
    }
  },

  // ── Export Data ──────────────────────────────────────
  async exportUsers() {
    try {
      const response = await api.get('/admin/export/users', { responseType: 'blob' });
      downloadBlob(response.data, 'unilost-users.csv');
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to export users' };
    }
  },

  async exportItems() {
    try {
      const response = await api.get('/admin/export/items', { responseType: 'blob' });
      downloadBlob(response.data, 'unilost-items.csv');
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to export items' };
    }
  },

  async exportAnalytics() {
    try {
      const response = await api.get('/admin/export/analytics', { responseType: 'blob' });
      downloadBlob(response.data, 'unilost-analytics.csv');
      return { success: true };
    } catch (err) {
      return { success: false, error: 'Failed to export analytics' };
    }
  },

  // ── Audit Logs ────────────────────────────────────────
  async getAuditLogs(params = {}) {
    try {
      const response = await api.get('/admin/audit-logs', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch audit logs' };
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
