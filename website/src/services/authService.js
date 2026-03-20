import api from './api';
import { ROLES } from '../constants/roles';

const authService = {
    // Register a new user
    register: async (userData) => {
        try {
            const response = await api.post('/auth/register', {
                fullName: userData.fullName,
                email: userData.email,
                password: userData.password,
            });
            return { success: true, data: response.data };
        } catch (error) {
            const message = error.response?.data || error.message || 'Registration failed';
            return { success: false, error: message };
        }
    },

    // Login user by email and password
    login: async (email, password) => {
        try {
            const response = await api.post('/auth/login', {
                email,
                password
            });

            const { token, user } = response.data;
            if (token && user) {
                localStorage.setItem('user', JSON.stringify(user));
                localStorage.setItem('token', token);
                return { success: true, data: user };
            } else {
                return { success: false, error: 'Login failed' };
            }
        } catch (error) {
            const message = error.response?.data || error.message || 'Login failed';
            return { success: false, error: message };
        }
    },

    // Logout user
    logout: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
    },

    // Get current user from localStorage
    getCurrentUser: () => {
        try {
            const user = localStorage.getItem('user');
            return user ? JSON.parse(user) : null;
        } catch {
            localStorage.removeItem('user');
            return null;
        }
    },

    // Check if user is logged in and token is not expired
    isAuthenticated: () => {
        const token = localStorage.getItem('token');
        if (!token) return false;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            if (payload.exp && payload.exp * 1000 < Date.now()) {
                localStorage.removeItem('user');
                localStorage.removeItem('token');
                return false;
            }
            return true;
        } catch {
            // Malformed token — treat as unauthenticated
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            return false;
        }
    },

    // Get current user's role
    getUserRole: () => {
        try {
            const user = localStorage.getItem('user');
            if (user) {
                const parsed = JSON.parse(user);
                return parsed.role || ROLES.STUDENT;
            }
            return null;
        } catch {
            localStorage.removeItem('user');
            return null;
        }
    },

    // Check if current user is admin
    isAdmin: () => {
        const role = authService.getUserRole();
        return role === ROLES.ADMIN;
    },

    // Check if current user is faculty
    isFaculty: () => {
        return authService.getUserRole() === ROLES.FACULTY;
    },

    // Password reset flow
    forgotPassword: async (email) => {
        try {
            const response = await api.post('/auth/forgot-password', { email });
            return { success: true, message: response.data.message };
        } catch (error) {
            const message = error.response?.data || error.message || 'Failed to send reset code';
            return { success: false, error: message };
        }
    },

    verifyOtp: async (email, otp) => {
        try {
            const response = await api.post('/auth/verify-otp', { email, otp });
            return { success: true, message: response.data.message };
        } catch (error) {
            const message = error.response?.data || error.message || 'OTP verification failed';
            return { success: false, error: message };
        }
    },

    resetPassword: async (email, otp, newPassword) => {
        try {
            const response = await api.post('/auth/reset-password', { email, otp, newPassword });
            return { success: true, message: response.data.message };
        } catch (error) {
            const message = error.response?.data || error.message || 'Password reset failed';
            return { success: false, error: message };
        }
    },
};

export default authService;
