import { Navigate, Outlet } from 'react-router-dom';
import authService from '../services/authService';

export const AdminRoute = () => {
    const isAuthenticated = authService.isAuthenticated();
    const isAdmin = authService.isAdmin();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!isAdmin) {
        return <Navigate to="/items" replace />;
    }

    return <Outlet />;
};

export const SuperAdminRoute = () => {
    const isAuthenticated = authService.isAuthenticated();
    const isSuperAdmin = authService.isSuperAdmin();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!isSuperAdmin) {
        return <Navigate to="/admin" replace />;
    }

    return <Outlet />;
};
