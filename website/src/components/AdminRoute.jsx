import { Navigate, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import authService from "../services/authService";

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
      setIsAdmin(role === 'ADMIN' || role === 'FACULTY');
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

export const FacultyRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const [isChecking, setIsChecking] = useState(true);
  const [isFaculty, setIsFaculty] = useState(false);

  useEffect(() => {
    let mounted = true;

    const validateRole = async () => {
      if (!isAuthenticated) {
        if (mounted) {
          setIsFaculty(false);
          setIsChecking(false);
        }
        return;
      }

      const result = await authService.syncCurrentUser();
      if (!mounted) return;

      setIsFaculty(result.success && result.data?.role === 'FACULTY');
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

  if (!isFaculty) {
    return <Navigate to="/admin" replace />;
  }

  return <Outlet />;
};
