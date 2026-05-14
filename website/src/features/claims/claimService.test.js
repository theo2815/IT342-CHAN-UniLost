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
import claimService from './claimService';

describe('claimService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('submitClaim', () => {
    it('posts to /claims and returns the new claim DTO', async () => {
      api.post.mockResolvedValue({ data: { id: 'claim-1', status: 'PENDING' } });
      const result = await claimService.submitClaim({
        itemId: 'item-1', providedAnswer: 'Levi', message: 'mine!',
      });
      expect(result).toEqual({ success: true, data: { id: 'claim-1', status: 'PENDING' } });
      expect(api.post).toHaveBeenCalledWith('/claims', {
        itemId: 'item-1', providedAnswer: 'Levi', message: 'mine!',
      });
    });

    it('surfaces backend error message on rejection', async () => {
      api.post.mockRejectedValue({ response: { data: { error: 'Secret detail answer is required' } } });
      const result = await claimService.submitClaim({ itemId: 'item-1' });
      expect(result.success).toBe(false);
      expect(result.error).toBe('Secret detail answer is required');
    });
  });

  describe('getMyClaims', () => {
    it('unwraps Spring Page<T> shape into data + pagination metadata', async () => {
      api.get.mockResolvedValue({
        data: { content: [{ id: 'claim-1' }], totalPages: 5, totalElements: 100 },
      });

      const result = await claimService.getMyClaims(2, 20);

      expect(result).toEqual({
        success: true,
        data: [{ id: 'claim-1' }],
        totalPages: 5,
        totalElements: 100,
      });
      expect(api.get).toHaveBeenCalledWith('/claims/my?page=2&size=20');
    });
  });

  describe('getIncomingClaims', () => {
    it('uses /claims/incoming and unwraps the page', async () => {
      api.get.mockResolvedValue({
        data: { content: [], totalPages: 0, totalElements: 0 },
      });
      const result = await claimService.getIncomingClaims();
      expect(result.success).toBe(true);
      expect(api.get).toHaveBeenCalledWith('/claims/incoming?page=0&size=20');
    });
  });

  describe('handover state machine wrappers', () => {
    it.each([
      ['acceptClaim', '/claims/c1/accept'],
      ['rejectClaim', '/claims/c1/reject'],
      ['cancelClaim', '/claims/c1/cancel'],
      ['markItemReturned', '/claims/c1/mark-returned'],
      ['confirmItemReceived', '/claims/c1/confirm-received'],
      ['disputeHandover', '/claims/c1/dispute-handover'],
    ])('%s PUTs to %s', async (method, expectedUrl) => {
      api.put.mockResolvedValue({ data: { id: 'c1', status: 'OK' } });
      const result = await claimService[method]('c1');
      expect(result.success).toBe(true);
      expect(api.put).toHaveBeenCalledWith(expectedUrl);
    });

    it('confirmItemReceived returns failure when item is not pending owner confirmation', async () => {
      api.put.mockRejectedValue({
        response: { data: { error: 'The finder must mark the item as returned first' } },
      });
      const result = await claimService.confirmItemReceived('c1');
      expect(result.success).toBe(false);
      expect(result.error).toBe('The finder must mark the item as returned first');
    });
  });
});
