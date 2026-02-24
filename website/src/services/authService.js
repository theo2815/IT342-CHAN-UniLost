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
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    },

    // Check if user is logged in
    isAuthenticated: () => {
        return !!localStorage.getItem('token');
    },

    // Get current user's role
    getUserRole: () => {
        const user = localStorage.getItem('user');
        if (user) {
            const parsed = JSON.parse(user);
            return parsed.role || 'STUDENT';
        }
        return null;
    },

    // Check if current user is admin
    isAdmin: () => {
        const role = authService.getUserRole();
        return role === 'ADMIN' || role === 'SUPER_ADMIN';
    },

    // Check if current user is super admin
    isSuperAdmin: () => {
        return authService.getUserRole() === 'SUPER_ADMIN';
    },
};

export default authService;
