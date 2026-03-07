import { Navigate, Outlet } from "react-router-dom";
import authService from "../services/authService";

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

export const FacultyRoute = () => {
  const isAuthenticated = authService.isAuthenticated();
  const isFaculty = authService.isFaculty();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isFaculty) {
    return <Navigate to="/admin" replace />;
  }

  return <Outlet />;
};
