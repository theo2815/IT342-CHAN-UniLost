import { Navigate, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import authService from "../../features/auth/authService";

export const AdminRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const cachedUser = isAuthenticated ? authService.getCurrentUser() : null;
  const initialIsAdmin = cachedUser?.role === 'ADMIN';

  // Optimistic: trust the cached role to render immediately, then re-validate
  // against /auth/me in the background and correct state if the server disagrees.
  const [isAdmin, setIsAdmin] = useState(initialIsAdmin);

  useEffect(() => {
    if (!isAuthenticated) return;
    let mounted = true;

    authService.syncCurrentUser().then((result) => {
      if (!mounted) return;
      const role = result.success ? result.data?.role : null;
      const serverIsAdmin = role === 'ADMIN';
      if (serverIsAdmin !== initialIsAdmin) {
        setIsAdmin(serverIsAdmin);
      }
    });

    return () => {
      mounted = false;
    };
  }, [isAuthenticated, initialIsAdmin]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/items" replace />;
  }

  return <Outlet />;
};
