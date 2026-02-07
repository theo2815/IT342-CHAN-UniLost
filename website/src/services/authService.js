import api from './api';

const authService = {
    // Register a new user
    register: async (userData) => {
        try {
            const response = await api.post('/auth/register', {
                firstName: userData.firstName,
                lastName: userData.lastName,
                email: userData.email,
                password: userData.password,
                studentIdNumber: userData.studentIdNumber || null,
                address: userData.address || null,
                phoneNumber: userData.phoneNumber || null,
                profilePicture: userData.profilePicture || null,
                schoolId: userData.schoolId || null,
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
            
            const userData = response.data;
            if (userData) {
                 localStorage.setItem('user', JSON.stringify(userData));
                 return { success: true, data: userData };
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
    },

    // Get current user from localStorage
    getCurrentUser: () => {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    // Check if user is logged in
    isAuthenticated: () => {
        return !!localStorage.getItem('user');
    },
};

export default authService;
