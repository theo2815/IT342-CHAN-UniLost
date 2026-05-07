import { Navigate, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import authService from "../../features/auth/authService";

export const AdminRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const [isChecking, setIsChecking] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    let mounted = true;

    const validateRole = async () => {
      if (!isAuthenticated) {
        if (mounted) {
          setIsAdmin(false);
          setIsChecking(false);
        }
        return;
      }

      const result = await authService.syncCurrentUser();
      if (!mounted) return;

      const role = result.success ? result.data?.role : null;
      setIsAdmin(role === 'ADMIN');
      setIsChecking(false);
    };

    validateRole();
    return () => {
      mounted = false;
    };
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (isChecking) {
    return null;
  }

  if (!isAdmin) {
    return <Navigate to="/items" replace />;
  }

  return <Outlet />;
};
