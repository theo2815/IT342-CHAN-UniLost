import axios from 'axios';

const PUBLIC_AUTH_ENDPOINTS = [
    '/auth/login',
    '/auth/register',
    '/auth/forgot-password',
    '/auth/verify-otp',
    '/auth/reset-password',
];

// Base API configuration
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

// Request interceptor for adding auth token
api.interceptors.request.use(
    (config) => {
        const url = config.url || '';
        const isPublicAuthRequest = PUBLIC_AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));
        const token = localStorage.getItem('token');
        if (token && !isPublicAuthRequest) {
            config.headers.Authorization = `Bearer ${token}`;
        } else if (config.headers?.Authorization) {
            delete config.headers.Authorization;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            const url = error.config?.url || '';
            const isAuthRequest = PUBLIC_AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));
            const hasToken = !!localStorage.getItem('token');
            // Only redirect to login if the user had a token (session expired).
            // Guest users (no token) browsing public pages should not be redirected.
            if (!isAuthRequest && hasToken) {
                localStorage.removeItem('user');
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
