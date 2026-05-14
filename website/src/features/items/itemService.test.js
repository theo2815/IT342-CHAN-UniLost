import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../shared/services/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import api from '../../shared/services/api';
import itemService from './itemService';

describe('itemService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getItems', () => {
    it('returns paged content via { success: true, data } envelope', async () => {
      const page = { content: [{ id: 'item-1', title: 'Wallet' }], totalPages: 1, totalElements: 1 };
      api.get.mockResolvedValue({ data: page });

      const result = await itemService.getItems({ keyword: 'wallet', page: 0, size: 20 });

      expect(result).toEqual({ success: true, data: page });
      expect(api.get).toHaveBeenCalledWith('/items', { params: { keyword: 'wallet', page: 0, size: 20 } });
    });

    it('returns { success: false, error } when API rejects', async () => {
      api.get.mockRejectedValue({ response: { data: { error: 'boom' } } });
      const result = await itemService.getItems();
      expect(result.success).toBe(false);
      expect(result.error).toBe('boom');
    });

    it('falls back to a default error message when no body is returned', async () => {
      api.get.mockRejectedValue(new Error('Network error'));
      const result = await itemService.getItems();
      expect(result.success).toBe(false);
      expect(result.error).toBe('Failed to fetch items');
    });
  });

  describe('getItemById', () => {
    it('returns the single item DTO', async () => {
      const item = { id: 'item-1', title: 'Wallet', secretDetailQuestion: 'Brand?' };
      api.get.mockResolvedValue({ data: item });
      const result = await itemService.getItemById('item-1');
      expect(result).toEqual({ success: true, data: item });
      expect(api.get).toHaveBeenCalledWith('/items/item-1');
    });
  });

  describe('createItem', () => {
    it('sends multipart form data with item JSON + image files', async () => {
      api.post.mockResolvedValue({ data: { id: 'item-1' } });
      const fakeFile = new File(['x'], 'photo.jpg', { type: 'image/jpeg' });

      const result = await itemService.createItem(
        { title: 'Wallet', type: 'FOUND', category: 'WALLETS' },
        [fakeFile],
      );

      expect(result.success).toBe(true);
      const [url, formData] = api.post.mock.calls[0];
      expect(url).toBe('/items');
      expect(formData).toBeInstanceOf(FormData);
      expect(formData.has('item')).toBe(true);
      expect(formData.has('images')).toBe(true);
    });

    it('surfaces backend error.error field when present', async () => {
      api.post.mockRejectedValue({ response: { data: { error: 'Maximum 3 images allowed' } } });
      const result = await itemService.createItem({}, []);
      expect(result.success).toBe(false);
      expect(result.error).toBe('Maximum 3 images allowed');
    });
  });

  describe('updateItem', () => {
    it('PUTs multipart form data to /items/:id', async () => {
      api.put.mockResolvedValue({ data: { id: 'item-1', title: 'Updated' } });
      const result = await itemService.updateItem('item-1', { title: 'Updated' }, []);
      expect(result.success).toBe(true);
      expect(api.put).toHaveBeenCalledWith('/items/item-1', expect.any(FormData));
    });
  });

  describe('deleteItem', () => {
    it('returns { success: true } with no body on success', async () => {
      api.delete.mockResolvedValue({});
      const result = await itemService.deleteItem('item-1');
      expect(result).toEqual({ success: true });
      expect(api.delete).toHaveBeenCalledWith('/items/item-1');
    });

    it('returns { success: false } when delete is forbidden', async () => {
      api.delete.mockRejectedValue({ response: { data: { error: 'Cannot delete a claimed item' } } });
      const result = await itemService.deleteItem('item-1');
      expect(result.success).toBe(false);
      expect(result.error).toBe('Cannot delete a claimed item');
    });
  });

  describe('getMapItems', () => {
    it('passes campusId/type filters as query params', async () => {
      api.get.mockResolvedValue({ data: [] });
      await itemService.getMapItems({ campusId: 'campus-usc', type: 'FOUND' });
      expect(api.get).toHaveBeenCalledWith('/items/map', {
        params: { campusId: 'campus-usc', type: 'FOUND' },
      });
    });
  });
});
