import api from './api';

const itemService = {
  /**
   * Get items with search, filter, and pagination
   * @param {Object} params - { keyword, campusId, category, type, status, page, size }
   */
  async getItems(params = {}) {
    try {
      const response = await api.get('/items', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch items' };
    }
  },

  /**
   * Get a single item by ID
   */
  async getItemById(id) {
    try {
      const response = await api.get(`/items/${id}`);
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch item' };
    }
  },

  /**
   * Create a new item with optional image upload
   * @param {Object} itemData - Item fields
   * @param {File[]} images - Image files to upload
   */
  async createItem(itemData, images = []) {
    try {
      const formData = new FormData();
      formData.append('item', new Blob([JSON.stringify(itemData)], { type: 'application/json' }));
      images.forEach(img => formData.append('images', img));

      const response = await api.post('/items', formData);
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to create item' };
    }
  },

  /**
   * Update an existing item
   */
  async updateItem(id, itemData, images = []) {
    try {
      const formData = new FormData();
      formData.append('item', new Blob([JSON.stringify(itemData)], { type: 'application/json' }));
      images.forEach(img => formData.append('images', img));

      const response = await api.put(`/items/${id}`, formData);
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to update item' };
    }
  },

  /**
   * Soft delete an item
   */
  async deleteItem(id) {
    try {
      await api.delete(`/items/${id}`);
      return { success: true };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to delete item' };
    }
  },

  /**
   * Get all items posted by a specific user
   */
  async getItemsByUser(userId, params = {}) {
    try {
      const response = await api.get(`/items/user/${userId}`, { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch user items' };
    }
  },

  /**
   * Get items with coordinates for map view
   * @param {Object} params - { campusId, type }
   */
  async getMapItems(params = {}) {
    try {
      const response = await api.get('/items/map', { params });
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch map items' };
    }
  },

  /**
   * Get all items for a specific campus
   */
  async getItemsByCampus(campusId) {
    try {
      const response = await api.get(`/items/campus/${campusId}`);
      return { success: true, data: response.data };
    } catch (err) {
      return { success: false, error: err.response?.data?.error || err.response?.data || 'Failed to fetch campus items' };
    }
  },
};

export default itemService;
