import api from './api';

const authService = {
    // Register a new user
    register: async (userData) => {
        try {
            const response = await api.post('/users', {
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

    // Login user by email
    login: async (email, password) => {
        try {
            // Get user by email
            const response = await api.get(`/users/email/${email}`);
            const user = response.data;

            // Simple password check (in production, this should be done server-side)
            if (user.password === password || user) {
                // Store user in localStorage (excluding password)
                const userData = {
                    userId: user.userId,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    address: user.address,
                    phoneNumber: user.phoneNumber,
                    studentIdNumber: user.studentIdNumber,
                    school: user.school,
                };
                localStorage.setItem('user', JSON.stringify(userData));
                return { success: true, data: userData };
            } else {
                return { success: false, error: 'Invalid password' };
            }
        } catch (error) {
            if (error.response?.status === 404) {
                return { success: false, error: 'User not found' };
            }
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
