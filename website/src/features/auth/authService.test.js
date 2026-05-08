import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../shared/services/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import api from '../../shared/services/api';
import authService from './authService';

// Build a JWT-like string (header.payload.signature) with an exp claim.
function jwt({ exp, role = 'STUDENT' } = {}) {
  const expSeconds = exp ?? Math.floor(Date.now() / 1000) + 3600;
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'alice@usc.edu.ph', role, exp: expSeconds }));
  return `${header}.${payload}.signature`;
}

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('register', () => {
    it('returns { success: true, data } when API call succeeds', async () => {
      api.post.mockResolvedValue({ data: { id: 'user-1', email: 'alice@usc.edu.ph' } });

      const result = await authService.register({
        fullName: 'Alice', email: 'alice@usc.edu.ph', password: 'Pass1234!',
      });

      expect(result).toEqual({ success: true, data: { id: 'user-1', email: 'alice@usc.edu.ph' } });
      expect(api.post).toHaveBeenCalledWith('/auth/register', expect.objectContaining({
        email: 'alice@usc.edu.ph',
      }));
    });

    it('omits campusId when not provided (auto-assign single-domain campus)', async () => {
      api.post.mockResolvedValue({ data: {} });
      await authService.register({ fullName: 'Alice', email: 'a@usc.edu.ph', password: 'x' });
      expect(api.post).toHaveBeenCalledWith('/auth/register',
        expect.not.objectContaining({ campusId: expect.anything() }));
    });

    it('passes campusId when provided (multi-domain disambiguation)', async () => {
      api.post.mockResolvedValue({ data: {} });
      await authService.register({
        fullName: 'Alice', email: 'a@shared.edu.ph', password: 'x', campusId: 'campus-citu',
      });
      expect(api.post).toHaveBeenCalledWith('/auth/register',
        expect.objectContaining({ campusId: 'campus-citu' }));
    });

    it('returns { success: false, error } on API rejection', async () => {
      api.post.mockRejectedValue({ response: { data: 'Email already registered' } });

      const result = await authService.register({
        fullName: 'Alice', email: 'alice@usc.edu.ph', password: 'x',
      });

      expect(result).toEqual({ success: false, error: 'Email already registered' });
    });
  });

  describe('login', () => {
    it('persists token + user in localStorage on success', async () => {
      const token = jwt();
      const user = { id: 'user-1', email: 'alice@usc.edu.ph', role: 'STUDENT' };
      api.post.mockResolvedValue({ data: { token, user } });

      const result = await authService.login('alice@usc.edu.ph', 'Pass1234!');

      expect(result).toEqual({ success: true, data: user });
      expect(localStorage.getItem('token')).toBe(token);
      expect(JSON.parse(localStorage.getItem('user'))).toEqual(user);
    });

    it('returns { success: false } on credential failure', async () => {
      api.post.mockRejectedValue({ response: { data: 'Invalid email or password' } });

      const result = await authService.login('alice@usc.edu.ph', 'wrong');

      expect(result.success).toBe(false);
      expect(result.error).toBe('Invalid email or password');
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('logout', () => {
    it('clears token + user from localStorage', () => {
      localStorage.setItem('token', 'x');
      localStorage.setItem('user', JSON.stringify({ id: 'u' }));
      authService.logout();
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('returns true for an unexpired token', () => {
      localStorage.setItem('token', jwt());
      expect(authService.isAuthenticated()).toBe(true);
    });

    it('returns false and clears storage for an expired token', () => {
      localStorage.setItem('token', jwt({ exp: Math.floor(Date.now() / 1000) - 60 }));
      localStorage.setItem('user', JSON.stringify({ id: 'u' }));
      expect(authService.isAuthenticated()).toBe(false);
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });

    it('returns false and clears storage for a malformed token', () => {
      localStorage.setItem('token', 'not-a-jwt');
      localStorage.setItem('user', JSON.stringify({ id: 'u' }));
      expect(authService.isAuthenticated()).toBe(false);
      expect(localStorage.getItem('token')).toBeNull();
    });

    it('returns false when no token exists', () => {
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('isAdmin', () => {
    it('returns true when stored user role is ADMIN', () => {
      localStorage.setItem('token', jwt());
      localStorage.setItem('user', JSON.stringify({ id: 'u', role: 'ADMIN' }));
      expect(authService.isAdmin()).toBe(true);
    });

    it('returns false for STUDENT role', () => {
      localStorage.setItem('token', jwt());
      localStorage.setItem('user', JSON.stringify({ id: 'u', role: 'STUDENT' }));
      expect(authService.isAdmin()).toBe(false);
    });
  });

  describe('password reset flow', () => {
    it('forgotPassword wraps response.message into envelope', async () => {
      api.post.mockResolvedValue({ data: { message: 'Code sent' } });
      const result = await authService.forgotPassword('alice@usc.edu.ph');
      expect(result).toEqual({ success: true, message: 'Code sent' });
    });

    it('verifyOtp wraps full data + message', async () => {
      api.post.mockResolvedValue({ data: { resetToken: 'abc-123', message: 'Verified' } });
      const result = await authService.verifyOtp('alice@usc.edu.ph', '123456');
      expect(result.success).toBe(true);
      expect(result.data.resetToken).toBe('abc-123');
      expect(result.message).toBe('Verified');
    });

    it('resetPassword surfaces backend error on rejection', async () => {
      api.post.mockRejectedValue({ response: { data: 'Reset token has expired' } });
      const result = await authService.resetPassword('alice@usc.edu.ph', 'token', 'NewPass1234!');
      expect(result.success).toBe(false);
      expect(result.error).toBe('Reset token has expired');
    });
  });
});
